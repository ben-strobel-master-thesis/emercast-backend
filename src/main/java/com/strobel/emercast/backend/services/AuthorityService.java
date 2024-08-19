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
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.time.Instant;
import java.util.*;

@Service
public class AuthorityService {

    private static final Logger logger = LoggerFactory.getLogger(AuthorityService.class);

    @Autowired
    AuthorityRepository authorityRepository;

    @Autowired
    BroadcastMessageService broadcastMessageService;

    public static final UUID rootAuthorityUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @EventListener(ApplicationReadyEvent.class)
    public void createRootAuthorityIfNotExists() {
        var rootAuthority = authorityRepository.findById(new TUID<>(rootAuthorityUuid));
        if(rootAuthority.isPresent()) return;
        var password = UUID.randomUUID().toString();
        logger.info("Root authority password: {}", password);
        createAuthority(
                rootAuthorityUuid,
                "root",
                password,
                new TUID<>(rootAuthorityUuid),
                "Root Authority",
                "Root Authority",
                List.of(JurisdictionMarker.newCircleMarker(-48.1351,11.5820, 20000000L))
        );
    }

    public boolean checkCredentials(String username, String password) {
        var authority = authorityRepository.findByLoginName(username);
        if(authority.isEmpty()) return false;
        return BCrypt.checkpw(password, authority.get().getPasswordHash());
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

        var parent = uuid.equals(rootAuthorityUuid) ? Optional.of(authority) : authorityRepository.findById(createdBy);
        if(parent.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        signAuthorityByParent(authority, parent.get());

        authority.setPath(computeAuthorityPath(authority));
        broadcastMessageService.sendSystemBroadcastMessage(this, SystemMessageKindEnum.AUTHORITY_ISSUED, authority);

        return authorityRepository.save(authority);
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

        broadcastMessageService.sendSystemBroadcastMessage(
                this,
                SystemMessageKindEnum.AUTHORITY_REVOKED,
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
        // TODO Compute new keypair
        // TODO Sign new authority
        // TODO Send AuthorityRevoked, and AuthorityIssued messages (should be sent in one broadcastmessage, so both always are received at the same time)
    }

    public void signBroadcastMessageWithRootCertificate(BroadcastMessage broadcastMessage) {
        var rootAuthority = authorityRepository.findById(new TUID<>(rootAuthorityUuid));
        if(rootAuthority.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        signBroadcastMessage(rootAuthority.get(), broadcastMessage);
    }

    public void signBroadcastMessage(Authority authority, BroadcastMessage broadcastMessage) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            var messageHash = md.digest(broadcastMessage.getMessageBytesForDigest());
            var cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, authority.getPrivateKey());
            byte[] signedMessageHash = cipher.doFinal(messageHash);
            broadcastMessage.setIssuedAuthorityId(authority.getId());
            broadcastMessage.setIssuerSignature(Base64.getEncoder().encodeToString(signedMessageHash));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
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
        while(!current.getCreatedBy().equals(rootAuthorityUuid)) {
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
