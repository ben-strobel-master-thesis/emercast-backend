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
                .putData("id", broadcastMessage.getId().toString())
                .putData("title", broadcastMessage.getTitle())
                .putData("content", broadcastMessage.getMessage())
                .putData("category", broadcastMessage.getCategory())
                .putData("severity", broadcastMessage.getSeverity().toString())
                .putData("created", ""+broadcastMessage.getCreated().getEpochSecond())
                .putData("modified", ""+broadcastMessage.getModified().getEpochSecond())
                .putData("latitude", broadcastMessage.getLatitude().toString())
                .putData("longitude", broadcastMessage.getLongitude().toString())
                .putData("radius", broadcastMessage.getRadius().toString())
                .putData("forwardUntil", ""+broadcastMessage.getForwardUntil().getEpochSecond())
                .putData("issuedAuthorityId", ""+broadcastMessage.getIssuedAuthorityId())
                .putData("issuerSignature", broadcastMessage.getIssuerSignature())
                .setTopic("test")
                .build();

        try {
            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
