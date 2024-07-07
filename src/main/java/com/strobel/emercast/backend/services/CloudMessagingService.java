package com.strobel.emercast.backend.services;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.strobel.emercast.backend.db.models.BroadcastMessage;
import org.springframework.stereotype.Service;

@Service
public class CloudMessagingService {
    public void sendCloudMessagingMessage(BroadcastMessage broadcastMessage) {
        var firebaseMessaging = FirebaseMessaging.getInstance();

        Message message = Message.builder()
                .putData("title", broadcastMessage.getTitle())
                .putData("content", broadcastMessage.getTitle())
                .putData("category", broadcastMessage.getCategory())
                .putData("severity", broadcastMessage.getSeverity().toString())
                .putData("created", broadcastMessage.getCreated().toString())
                .putData("forwardUntil", broadcastMessage.getForwardUntil().toString())
                .setTopic("test")
                .build();

        try {
            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
