package com.strobel.emercast.backend.services;

import com.strobel.emercast.backend.db.models.BroadcastMessage;
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

    public BroadcastMessage sendBroadcastMessage(Float latitude, Float longitude, Integer radius, String category, Integer severity, String title, String messageContent) {
        var message = BroadcastMessage.newInstance(latitude, longitude, radius, category, severity, title, messageContent);
        this.broadcastMessageRepository.save(message);
        cloudMessagingService.sendCloudMessagingMessage(message);
        return message;
    }
}
