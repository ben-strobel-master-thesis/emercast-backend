package com.strobel.emercast.backend.db.models.authority;

import com.strobel.emercast.backend.db.models.base.TUID;
import com.strobel.emercast.backend.db.models.base.UuidEntity;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "authorities")
public class Authority extends UuidEntity<Authority> {

    private String loginName;
    private String passwordHash;

    private Instant created;
    private TUID<Authority> createdBy;

    private String publicName;
    private String jurisdictionDescription;

    private List<JurisdictionMarker> jurisdictionMarkers;

    private Instant keyPairValidUntil;
    private String publicKeyPath;
    private String privateKeyPath;
}
