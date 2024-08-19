package com.strobel.emercast.backend.services;

import com.strobel.emercast.backend.db.models.BroadcastMessage;
import com.strobel.emercast.backend.db.models.authority.Authority;
import com.strobel.emercast.backend.db.models.enums.SystemMessageKindEnum;
import com.strobel.emercast.backend.db.repositories.BroadcastMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    public BroadcastMessage sendSystemBroadcastMessage(AuthorityService authorityService, SystemMessageKindEnum kind, Authority authority) {
        // TODO create json message
        var message = BroadcastMessage.newInstance(0f, 0f, 0, "system", 0, kind.name(), "TODO");
        message.setSystemMessage(true);
        authorityService.signBroadcastMessageWithRootCertificate(message);
        this.broadcastMessageRepository.save(message);
        cloudMessagingService.sendCloudMessagingMessage(message);
        return message;
    }
}
