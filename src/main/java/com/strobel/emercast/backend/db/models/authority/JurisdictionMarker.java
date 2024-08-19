package com.strobel.emercast.backend.db.models.authority;

import com.openapi.gen.springboot.dto.JurisdictionMarkerDTO;
import com.strobel.emercast.backend.db.models.enums.JurisdictionMarkerKindEnum;

public class JurisdictionMarker {

    public static JurisdictionMarker newCircleMarker(Double latitude, Double longitude, Long radiusMeters) {
        var marker = new JurisdictionMarker();
        marker.latitude = latitude;
        marker.kind = JurisdictionMarkerKindEnum.CIRCLE;
        marker.longitude = longitude;
        marker.radiusMeters = radiusMeters;
        return marker;
    }

    private JurisdictionMarkerKindEnum kind;

    private Double latitude;
    private Double longitude;
    private Long radiusMeters;

    @Override
    public String toString() {
        return "JurisdictionMarker{" +
                "kind=" + kind +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", radiusMeters=" + radiusMeters +
                '}';
    }

    public JurisdictionMarkerKindEnum getKind() {
        return kind;
    }

    public void setKind(JurisdictionMarkerKindEnum kind) {
        this.kind = kind;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Long getRadiusMeters() {
        return radiusMeters;
    }

    public void setRadiusMeters(Long radiusMeters) {
        this.radiusMeters = radiusMeters;
    }

    public JurisdictionMarkerDTO toOpenAPI() {
        return new JurisdictionMarkerDTO(
                this.getLatitude().floatValue(),
                this.getLongitude().floatValue(),
                this.getKind().toOpenAPI(),
                this.getRadiusMeters().intValue()
        );
    }
}
