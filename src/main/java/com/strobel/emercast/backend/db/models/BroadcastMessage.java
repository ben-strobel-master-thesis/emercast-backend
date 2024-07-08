package com.strobel.emercast.backend.db.models;

import com.openapi.gen.springboot.dto.AuthorityDTO;
import com.openapi.gen.springboot.dto.BroadcastMessageDTO;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Document(collection = "broadcast_message")
public class BroadcastMessage extends UuidEntity<BroadcastMessage> {

    public static BroadcastMessage newInstance(Float latitude, Float longitude, Float radius, String category, Integer severity, String title, String messageContent) {
        var message = new BroadcastMessage();
        message.setId(new TUID<>(UUID.randomUUID()));
        message.setCreated(Instant.now());
        message.setModified(Instant.now());
        message.setForwardUntil(Instant.now().plus(2, ChronoUnit.DAYS));
        message.setLatitude(latitude);
        message.setLongitude(longitude);
        message.setRadius(radius);
        message.setCategory(category);
        message.setSeverity(severity);
        message.setTitle(title);
        message.setMessage(messageContent);
        return message;
    }
    private Instant created;
    private Instant modified;
    private Instant forwardUntil;
    private Float latitude;
    private Float longitude;
    private Float radius;
    private String category;
    private Integer severity;
    private String title;
    private String message;

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public Instant getModified() {
        return modified;
    }

    public void setModified(Instant modified) {
        this.modified = modified;
    }

    public Instant getForwardUntil() {
        return forwardUntil;
    }

    public void setForwardUntil(Instant forwardUntil) {
        this.forwardUntil = forwardUntil;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public Float getRadius() {
        return radius;
    }

    public void setRadius(Float radius) {
        this.radius = radius;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BroadcastMessageDTO toOpenAPI() {
        return new BroadcastMessageDTO(
            this.getId().toOpenAPI(),
            this.getCreated().getEpochSecond(),
            this.getModified().getEpochSecond(),
            this.getLatitude(),
            this.getLongitude(),
            this.getRadius(),
            this.getCategory(),
            this.getSeverity(),
            this.getTitle(),
            this.getMessage(),
            "",
            new AuthorityDTO(UUID.randomUUID(), UUID.randomUUID(), "Default Authority")
        );
    }
}
