package com.strobel.emercast.backend.db.models.authority;

import com.strobel.emercast.backend.db.models.enums.JurisdictionMarkerKindEnum;

public class JurisdictionMarker {

    private JurisdictionMarkerKindEnum kind;

    private Double latitude;
    private Double longitude;
    private Double radiusMeters;
}
