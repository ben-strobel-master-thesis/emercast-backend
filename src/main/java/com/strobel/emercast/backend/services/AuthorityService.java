package com.strobel.emercast.backend.services;

import com.strobel.emercast.backend.db.models.BroadcastMessage;
import com.strobel.emercast.backend.db.models.authority.Authority;
import com.strobel.emercast.backend.db.models.authority.JurisdictionMarker;
import com.strobel.emercast.backend.db.models.base.TUID;
import com.strobel.emercast.backend.db.models.enums.SystemMessageKindEnum;
import com.strobel.emercast.backend.db.repositories.AuthorityRepository;
import com.strobel.emercast.backend.lib.PaginationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class AuthorityService {

    private static final Logger logger = LoggerFactory.getLogger(AuthorityService.class);

    private final AuthorityRepository authorityRepository;

    private final BroadcastMessageService broadcastMessageService;

    private final JwtService jwtService;

    private AuthorityService(
            @Autowired AuthorityRepository authorityRepository,
            @Autowired BroadcastMessageService broadcastMessageService,
            @Autowired JwtService jwtService
    ) {
        this.authorityRepository = authorityRepository;
        this.broadcastMessageService = broadcastMessageService;
        this.jwtService = jwtService;
    }

    public static final UUID rootAuthorityUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @EventListener(ApplicationReadyEvent.class)
    public void createRootAuthorityIfNotExists() {
        var rootAuthority = authorityRepository.findById(new TUID<>(rootAuthorityUuid));
        if (rootAuthority.isPresent()) return;
        var password = UUID.randomUUID().toString();
        logger.info("Root authority password: {}", password);
        createAuthority(
                rootAuthorityUuid,
                "root",
                password,
                new TUID<>(rootAuthorityUuid),
                "Root Authority",
                "Root Authority",
                List.of(JurisdictionMarker.newCircleMarker(-48.1351, 11.5820, 20000000L))
        );
    }

    public List<Authority> getAuthorityPage(Pageable pageable) {
        return authorityRepository.getAuthoritiesPage(Instant.now(), pageable);
    }

    public Authority createAuthority(
            String loginName,
            String password,
            TUID<Authority> createdBy,
            String publicName,
            String jurisdictionDescription,
            List<JurisdictionMarker> jurisdictionMarkers
    ) {
        return createAuthority(UUID.randomUUID(), loginName, password, createdBy, publicName, jurisdictionDescription, jurisdictionMarkers);
    }

    public Authority createAuthority(
            UUID uuid,
            String loginName,
            String password,
            TUID<Authority> createdBy,
            String publicName,
            String jurisdictionDescription,
            List<JurisdictionMarker> jurisdictionMarkers
    ) {
        var keyPair = generateKeyPair();

        var authority = Authority.newInstance(
                uuid,
                loginName,
                password,
                createdBy,
                publicName,
                jurisdictionDescription,
                jurisdictionMarkers,
                keyPair.getFirst(),
                keyPair.getSecond()
        );

        if(uuid.equals(rootAuthorityUuid)) {
            authority.setKeyPairValidUntil(Instant.now().plus(1000*365, ChronoUnit.DAYS));
        }

        var parent = uuid.equals(rootAuthorityUuid) ? Optional.of(authority) : authorityRepository.findById(createdBy);
        if(parent.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        signAuthorityByParent(authority, parent.get());

        authority.setPath(computeAuthorityPath(authority));
        var result = authorityRepository.save(authority);
        broadcastMessageService.sendSystemBroadcastAuthorityIssuedMessage(this, authority);

        return result;
    }

    public void revokeAuthority(TUID<Authority> id) {
        var authority = authorityRepository.findById(id);
        if(authority.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        var revocationTime = Instant.now();

        PaginationUtils.doForEveryPage(
                20,
                (pageable) -> authorityRepository.findByPathContainingSortedByPathIndex(id, false, pageable),
                (page) -> {
                    for (Authority a : page) {
                        a.setRevoked(revocationTime);
                    }
                    authorityRepository.saveAll(page);
                }
        );

        broadcastMessageService.sendSystemBroadcastAuthorityRevokedMessage(
                this,
                authority.get()
        );
    }

    public void renewAuthority(Authority authority) {

        revokeAuthority(authority.getId());

        PaginationUtils.doForEveryPagedItem(
                20,
                (pageable -> authorityRepository.findByPathContainingSortedByPathIndex(authority.getId(), true, pageable)),
                (item) -> {
                    var parent = authorityRepository.findById(item.getCreatedBy());
                    if(parent.isEmpty()) return;
                    createAuthority(
                            item.getId().toOpenAPI(),
                            item.getLoginName(),
                            item.getPasswordHash(),
                            item.getCreatedBy(),
                            item.getPublicName(),
                            item.getJurisdictionDescription(),
                            item.getJurisdictionMarkers()
                    );
                }
        );
    }

    public void renewExpiringAuthorities() {

        var timestampInAMonth = Instant.now().plus(30, ChronoUnit.DAYS);

        var first = true;
        Optional<Authority> currentAuthority = Optional.empty();
        while(first || currentAuthority.isPresent()) {
            first = false;
            currentAuthority = authorityRepository.findByKeyPairValidUntilBefore(timestampInAMonth);
            if(currentAuthority.isEmpty()) continue;
            renewAuthority(currentAuthority.get());
        }
    }

    public void signBroadcastMessageWithRootCertificate(BroadcastMessage broadcastMessage) {
        var rootAuthority = authorityRepository.findById(new TUID<>(rootAuthorityUuid));
        if(rootAuthority.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        signBroadcastMessage(rootAuthority.get(), broadcastMessage);
    }

    public Optional<Authority> checkCredentials(String username, String password) {
        var authority = authorityRepository.findByLoginName(username);
        if(authority.isEmpty()) return Optional.empty();
        if(BCrypt.checkpw(password, authority.get().getPasswordHash())) {
            return authority;
        }
        return Optional.empty();
    }

    public Authority getCallingAuthority() {
        var authority = authorityRepository.findById(jwtService.getCallingAuthorityId());
        if(authority.isEmpty()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return authority.get();
    }

    public void signBroadcastMessage(Authority authority, BroadcastMessage broadcastMessage) {
        try {
            broadcastMessage.setIssuedAuthorityId(authority.getId());
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            var messageHash = md.digest(broadcastMessage.getMessageBytesForDigest());
            var cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, authority.getPrivateKey());
            byte[] signedMessageHash = cipher.doFinal(messageHash);
            broadcastMessage.setIssuerSignature(Base64.getEncoder().encodeToString(signedMessageHash));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isRadiusWithinJurisdiction(List<JurisdictionMarker> jurisdictionMarkers, float latitude, float longitude, float radius) {

    }

    private void signAuthorityByParent(Authority authority, Authority parent) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            var messageHash = md.digest(authority.getMessageBytesForDigest());
            var cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, parent.getPrivateKey());
            byte[] signedMessageHash = cipher.doFinal(messageHash);
            authority.setCreatorSignature(Base64.getEncoder().encodeToString(signedMessageHash));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private List<TUID<Authority>> computeAuthorityPath(Authority authority) {
        var path = new ArrayList<TUID<Authority>>();
        var current = authority;
        while(!current.getId().equals(new TUID<>(rootAuthorityUuid))) {
            var parent = authorityRepository.findById(current.getCreatedBy());
            if(parent.isEmpty()) return new ArrayList<>();
            path.add(parent.get().getId());
            current = parent.get();
        }

        Collections.reverse(path);
        return path;
    }

    private Pair<String, String> generateKeyPair() {
        KeyPairGenerator gen = null;
        try {
            gen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        gen.initialize(2048);
        KeyPair keyPair = gen.generateKeyPair();

        PublicKey publicKey = keyPair.getPublic();
        String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());

        PrivateKey privateKey = keyPair.getPrivate();
        String privateKeyString = Base64.getEncoder().encodeToString(privateKey.getEncoded());

        return Pair.of(publicKeyString, privateKeyString);
    }
}
