package com.strobel.emercast.backend.services;

import com.strobel.emercast.backend.db.models.authority.Authority;
import com.strobel.emercast.backend.db.models.base.TUID;
import org.springframework.security.core.context.SecurityContextHolder;

public class JwtService {

    public String createTokenForAuthority(Authority authority) {
        Jwt
    }

    public TUID<Authority> getCallingAuthorityId() {
        SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
