package com.strobel.emercast.backend.services;

import com.strobel.emercast.backend.db.models.BroadcastMessage;
import com.strobel.emercast.backend.db.models.authority.Authority;
import com.strobel.emercast.backend.db.models.enums.SystemMessageKindEnum;
import com.strobel.emercast.backend.db.repositories.BroadcastMessageRepository;
import com.strobel.emercast.backend.lib.SerializationUtils;
import com.strobel.emercast.protobuf.SystemBroadcastMessageAuthorityIssuedPayloadPBO;
import com.strobel.emercast.protobuf.SystemBroadcastMessageAuthorityRevokedPayloadPBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;

@Service
public class BroadcastMessageService {

    private final BroadcastMessageRepository broadcastMessageRepository;
    private final CloudMessagingService cloudMessagingService;

    public BroadcastMessageService(
            @Autowired BroadcastMessageRepository broadcastMessageRepository,
            @Autowired CloudMessagingService cloudMessagingService) {
        this.broadcastMessageRepository = broadcastMessageRepository;
        this.cloudMessagingService = cloudMessagingService;
    }

    public BroadcastMessage sendPayloadBroadcastMessage(AuthorityService authorityService, Authority authority, Float latitude, Float longitude, Integer radius, String category, Integer severity, String title, String messageContent) {
        if(messageContent.length() > 4000) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        var message = BroadcastMessage.newInstance(latitude, longitude, radius, category, severity, title, messageContent);
        authorityService.signBroadcastMessage(authority, message);
        this.broadcastMessageRepository.save(message);
        cloudMessagingService.sendCloudMessagingMessage(message);
        return message;
    }

    public BroadcastMessage sendSystemBroadcastAuthorityRevokedMessage(AuthorityService authorityService, Authority authority) {
        var broadcastUnderJurisdictionWithYoungestForwardUntil = broadcastMessageRepository.findUnderJurisdictionWithYoungestForwardUntil(authority.getId());
        var canBeDeletedFrom = broadcastUnderJurisdictionWithYoungestForwardUntil.isPresent() ?
                broadcastUnderJurisdictionWithYoungestForwardUntil.get().getForwardUntil() :
                Instant.now();
        var payload = SystemBroadcastMessageAuthorityRevokedPayloadPBO.newBuilder()
                .setAuthorityId(authority.getId().toString())
                .setRevokedDate(authority.getRevoked().getEpochSecond())
                .setCanBeDeletedAt(canBeDeletedFrom.getEpochSecond())
                .build();
        var message = BroadcastMessage.newInstance(
                0f,
                0f,
                0,
                "system",
                0,
                SystemMessageKindEnum.AUTHORITY_REVOKED.name(),
                SerializationUtils.toBase64String(payload)
        );
        message.setSystemMessageRegardingAuthority(authority.getId());
        message.setForwardUntil(Instant.now().plus(1000*365, ChronoUnit.DAYS));
        message.setSystemMessage(true);
        authorityService.signBroadcastMessageWithRootCertificate(message);

        broadcastMessageRepository.setForwardUntilOverrideForSystemMessagesRegardingAuthorityWithTitle(authority.getId(), SystemMessageKindEnum.AUTHORITY_ISSUED.name(), canBeDeletedFrom);

        broadcastMessageRepository.save(message);
        cloudMessagingService.sendCloudMessagingMessage(message);
        return message;
    }

    public BroadcastMessage sendSystemBroadcastAuthorityIssuedMessage(AuthorityService authorityService, Authority authority) {
        var payload = SystemBroadcastMessageAuthorityIssuedPayloadPBO.newBuilder()
                .setAuthority(authority.toProtobuf())
                .build();
        var message = BroadcastMessage.newInstance(
                0f,
                0f,
                0,
                "system",
                0,
                SystemMessageKindEnum.AUTHORITY_ISSUED.name(),
                SerializationUtils.toBase64String(payload)
        );
        message.setSystemMessageRegardingAuthority(authority.getId());
        message.setForwardUntil(Instant.now().plus(1000*365, ChronoUnit.DAYS));
        message.setSystemMessage(true);
        authorityService.signBroadcastMessageWithRootCertificate(message);
        this.broadcastMessageRepository.save(message);
        cloudMessagingService.sendCloudMessagingMessage(message);
        return message;
    }

    public String getCurrentChainHash(boolean systemMessage) {
        var chainHashInput = broadcastMessageRepository.getCurrentChainHashInput(Instant.now(), systemMessage);
        if(chainHashInput == null) chainHashInput = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(md.digest(chainHashInput.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public List<BroadcastMessage> getPaginatedBroadcastMessages(Pageable pageable, boolean systemMessage) {
        return broadcastMessageRepository.findByForwardUntilBeforeAndSystemMessageIs(Instant.now(), systemMessage, pageable);
    }
}
