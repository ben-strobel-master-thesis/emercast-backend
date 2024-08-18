package com.strobel.emercast.backend.services;

import com.strobel.emercast.backend.db.models.BroadcastMessage;
import com.strobel.emercast.backend.db.models.authority.Authority;
import com.strobel.emercast.backend.db.models.enums.SystemMessageKindEnum;
import com.strobel.emercast.backend.db.repositories.BroadcastMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    // TODO Check total byte size < 4000 -> otherwise, return error
    public BroadcastMessage sendPayloadBroadcastMessage(Float latitude, Float longitude, Float radius, String category, Integer severity, String title, String messageContent) {
        var message = BroadcastMessage.newInstance(latitude, longitude, radius, category, severity, title, messageContent);
        this.broadcastMessageRepository.save(message);
        cloudMessagingService.sendCloudMessagingMessage(message);
        return message;
    }

    public BroadcastMessage sendSystemBroadcastMessage(SystemMessageKindEnum kind, Authority authority) {
        // TODO create json message
        var message = BroadcastMessage.newInstance(null, null, null, "system", 0, kind.name(), "TODO");
        message.setSystemMessage(true);
        this.broadcastMessageRepository.save(message);
        cloudMessagingService.sendCloudMessagingMessage(message);
        return message;
    }
}
