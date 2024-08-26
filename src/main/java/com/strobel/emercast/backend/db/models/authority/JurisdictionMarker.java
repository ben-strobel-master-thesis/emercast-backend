package com.strobel.emercast.backend.db.models.authority;

import com.openapi.gen.springboot.dto.JurisdictionMarkerDTO;
import com.strobel.emercast.backend.db.models.enums.JurisdictionMarkerKindEnum;
import com.strobel.emercast.protobuf.JurisdictionMarkerPBO;

public class JurisdictionMarker {

    public static JurisdictionMarker newCircleMarker(Double latitude, Double longitude, Long radiusMeters) {
        var marker = new JurisdictionMarker();
        marker.latitude = latitude;
        marker.longitude = longitude;
        marker.kind = JurisdictionMarkerKindEnum.CIRCLE;
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

    public JurisdictionMarkerPBO toProtobuf() {
        return JurisdictionMarkerPBO.newBuilder()
                .setLatitude(this.getLatitude().floatValue())
                .setLongitude(this.getLongitude().floatValue())
                .setKind(this.getKind().name())
                .setRadiusMeter(this.getRadiusMeters().intValue())
                .build();
    }

    public static JurisdictionMarker fromOpenAPI(JurisdictionMarkerDTO dto) {
        return newCircleMarker(dto.getLatitude().doubleValue(), dto.getLongitude().doubleValue(), dto.getRadiusMeter().longValue());
    }
}
