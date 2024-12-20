package com.strobel.emercast.backend.db.models.authority;

import com.openapi.gen.springboot.dto.AuthorityDTO;
import com.openapi.gen.springboot.dto.JurisdictionMarkerDTO;
import com.strobel.emercast.backend.db.models.base.TUID;
import com.strobel.emercast.backend.db.models.base.UuidEntity;
import com.strobel.emercast.protobuf.AuthorityPBO;
import org.checkerframework.checker.units.qual.A;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Document(collection = "authorities")
public class Authority extends UuidEntity<Authority> {

    public static Authority newInstance(UUID uuid, String loginName, String password, TUID<Authority> createdBy, String publicName, String jurisdictionDescription, List<JurisdictionMarker> jurisdictionMarkers, String publicKeyBase64, String privateKeyBase64) {
        var authority = new Authority();
        authority.id = new TUID<>(uuid);
        authority.loginName = loginName;
        authority.passwordHash = new BCryptPasswordEncoder().encode(password.subSequence(0, password.length()));
        authority.created = Instant.now();
        authority.createdBy = createdBy;
        authority.publicName = publicName;
        authority.jurisdictionDescription = jurisdictionDescription;
        authority.jurisdictionMarkers = jurisdictionMarkers;
        authority.keyPairValidUntil = Instant.now().plus(365, ChronoUnit.DAYS);
        authority.publicKeyBase64 = publicKeyBase64;
        authority.privateKeyBase64 = privateKeyBase64;

        // path and creatorSignature must also be set, are set in service that calls this function

        return authority;
    }

    @Indexed(unique = true)
    private String loginName;
    private String passwordHash;

    private Instant created;
    private TUID<Authority> createdBy;
    private String creatorSignature;
    private Instant revoked;

    // Not being sent to clients, is redundant data, is computed in advance to make other work more efficient
    private List<TUID<Authority>> path;

    private String publicName;
    private String jurisdictionDescription;

    private List<JurisdictionMarker> jurisdictionMarkers;

    private Instant keyPairValidUntil;
    private String publicKeyBase64;
    private String privateKeyBase64;

    public byte[] getMessageBytesForDigest() {
        return getMessageStringForDigest().getBytes(StandardCharsets.UTF_8);
    }

    // For production: This should be extracted into a shared library between server & clients
    public String getMessageStringForDigest() {
        var builder = new StringBuilder();
        builder.append(created.getEpochSecond());
        builder.append(createdBy.toString());
        builder.append(publicName);
        jurisdictionMarkers.forEach(m -> builder.append(m.toString()));
        builder.append(keyPairValidUntil.getEpochSecond());
        builder.append(publicKeyBase64);
        return builder.toString();
    }

    public String getJurisdictionDescription() {
        return jurisdictionDescription;
    }

    public void setJurisdictionDescription(String jurisdictionDescription) {
        this.jurisdictionDescription = jurisdictionDescription;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public TUID<Authority> getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(TUID<Authority> createdBy) {
        this.createdBy = createdBy;
    }

    public String getPublicName() {
        return publicName;
    }

    public void setPublicName(String publicName) {
        this.publicName = publicName;
    }

    public List<JurisdictionMarker> getJurisdictionMarkers() {
        return jurisdictionMarkers;
    }

    public void setJurisdictionMarkers(List<JurisdictionMarker> jurisdictionMarkers) {
        this.jurisdictionMarkers = jurisdictionMarkers;
    }

    public Instant getKeyPairValidUntil() {
        return keyPairValidUntil;
    }

    public void setKeyPairValidUntil(Instant keyPairValidUntil) {
        this.keyPairValidUntil = keyPairValidUntil;
    }

    public String getPublicKeyBase64() {
        return publicKeyBase64;
    }

    public void setPublicKeyBase64(String publicKeyBase64) {
        this.publicKeyBase64 = publicKeyBase64;
    }

    public PublicKey getPublicKey() {
        var decoded = Base64.getDecoder().decode(publicKeyBase64);
        try {
            var keyFactory = KeyFactory.getInstance("RSA");
            var publicKeySpec = new X509EncodedKeySpec(decoded);
            return keyFactory.generatePublic(publicKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public String getPrivateKeyBase64() {
        return privateKeyBase64;
    }

    public PrivateKey getPrivateKey() {
        var decoded = Base64.getDecoder().decode(privateKeyBase64);
        try {
            var keyFactory = KeyFactory.getInstance("RSA");
            var privateKeySpec = new PKCS8EncodedKeySpec(decoded);
            return keyFactory.generatePrivate(privateKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPrivateKeyBase64(String privateKeyBase64) {
        this.privateKeyBase64 = privateKeyBase64;
    }

    public String getCreatorSignature() {
        return creatorSignature;
    }

    public void setCreatorSignature(String creatorSignature) {
        this.creatorSignature = creatorSignature;
    }

    public List<TUID<Authority>> getPath() {
        return path;
    }

    public void setPath(List<TUID<Authority>> path) {
        this.path = path;
    }

    public Instant getRevoked() {
        return revoked;
    }

    public boolean isRevoked() {
        return revoked != null;
    }

    public void setRevoked(Instant revoked) {
        this.revoked = revoked;
    }

    public AuthorityDTO toOpenAPI() {
        return new AuthorityDTO(
                this.getId().toOpenAPI(),
                this.getCreated().getEpochSecond(),
                this.getCreatedBy().toOpenAPI(),
                this.getPublicName(),
                this.getJurisdictionMarkers().stream().map(JurisdictionMarker::toOpenAPI).collect(Collectors.toList()),
                this.getKeyPairValidUntil().getEpochSecond(),
                this.getPublicKeyBase64()
        );
    }

    public AuthorityPBO toProtobuf() {
        return AuthorityPBO.newBuilder()
                .setId(this.getId().toString())
                .setCreated(this.getCreated().getEpochSecond())
                .setCreatedBy(this.getCreatedBy().toString())
                .setPublicName(this.getPublicName())
                .addAllJurisdictionMarkers(this.getJurisdictionMarkers().stream().map(JurisdictionMarker::toProtobuf).collect(Collectors.toList()))
                .setKeyPairValidUntil(this.getKeyPairValidUntil().getEpochSecond())
                .setPublicKeyBase64(this.getPublicKeyBase64())
                .build();
    }
}
