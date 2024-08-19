package com.strobel.emercast.backend.db.models.enums;

import com.openapi.gen.springboot.dto.JurisdictionMarkerDTO;
import com.openapi.gen.springboot.dto.JurisdictionMarkerKindEnumDTO;

public enum JurisdictionMarkerKindEnum {
    CIRCLE;

    public JurisdictionMarkerKindEnumDTO toOpenAPI() {
        switch (this) {
            case CIRCLE -> {
                return JurisdictionMarkerKindEnumDTO.CIRCLE;
            }
        }
        throw new IllegalArgumentException("Unmapped enum: " + this);
    }
}
