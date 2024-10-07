package com.strobel.emercast.backend.services;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.strobel.emercast.backend.db.models.BroadcastMessage;
import com.strobel.emercast.backend.lib.LocationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CloudMessagingService {

    private static final Logger logger = LoggerFactory.getLogger(CloudMessagingService.class);

    private final double geoAccuracyDegree = 0.1;
    private final double geoAccuracyMeters = LocationUtils.distance(0, 0, 0, geoAccuracyDegree);

    private final String systemTopic = "system";

    public void sendCloudMessagingMessage(BroadcastMessage broadcastMessage) {
        var firebaseMessaging = FirebaseMessaging.getInstance();

        Message.Builder message = Message.builder()
                .putData("id", broadcastMessage.getId().toString())
                .putData("created", ""+broadcastMessage.getCreated().getEpochSecond())
                .putData("systemMessage", ""+broadcastMessage.getSystemMessage())
                .putData("forwardUntil", ""+broadcastMessage.getForwardUntil().getEpochSecond())
                .putData("latitude", broadcastMessage.getLatitude().toString())
                .putData("longitude", broadcastMessage.getLongitude().toString())
                .putData("radius", broadcastMessage.getRadius().toString())
                .putData("category", broadcastMessage.getCategory())
                .putData("severity", broadcastMessage.getSeverity().toString())
                .putData("title", broadcastMessage.getTitle())
                .putData("content", broadcastMessage.getMessage())
                .putData("issuedAuthorityId", ""+broadcastMessage.getIssuedAuthorityId())
                .putData("issuerSignature", broadcastMessage.getIssuerSignature());
        if(broadcastMessage.getSystemMessageRegardingAuthority() != null) {
            message = message.putData("systemMessageRegardingAuthority", broadcastMessage.getSystemMessageRegardingAuthority().toString());
        }

        var finalMessage = message;
        if(broadcastMessage.getSystemMessage()) {
            finalMessage.setTopic(systemTopic);
            try {
                logger.info("Sending broadcast message {} to topic {}", broadcastMessage.getId(), systemTopic);
                firebaseMessaging.send(finalMessage.build());
            } catch (FirebaseMessagingException e) {
                throw new RuntimeException(e);
            }
        } else {
            LocationUtils.doForEachSampleOfCircle(broadcastMessage.getLatitude(), broadcastMessage.getLongitude(), broadcastMessage.getRadius(), geoAccuracyMeters, (sample) -> {
                var topic = getTopicNameFromLatLong(sample.getFirst(), sample.getSecond());
                finalMessage.setTopic(topic);

                try {
                    logger.info("Sending broadcast message {} to topic {}", broadcastMessage.getId(), topic);
                    firebaseMessaging.send(finalMessage.build());
                } catch (FirebaseMessagingException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public Double roundToNearestPointFive(Double value) {
        double factor = 1.0/geoAccuracyDegree;
        return Math.round(value * factor) / factor;
    }

    public String getTopicNameFromLatLong(Double latitude, Double longitude) {
        return roundToNearestPointFive(latitude) + "_" + roundToNearestPointFive(longitude);
    }
}
