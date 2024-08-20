package com.strobel.emercast.backend.services;

import com.strobel.emercast.backend.db.models.authority.Authority;
import com.strobel.emercast.backend.db.models.base.TUID;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${emercast.jwt.private-key}")
    private String jwtPrivateKey;

    @Value("${emercast.jwt.expiration}")
    private long jwtExpiration;

    public String createTokenForAuthority(Authority authority) {
        return Jwts.builder()
                .subject(authority.getId().toString())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(SignatureAlgorithm.HS256, jwtPrivateKey)
                .compact();
    }

    public TUID<Authority> getCallingAuthorityId() {
        return new TUID<>(UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName()));
    }
}
