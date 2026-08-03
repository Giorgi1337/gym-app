package com.gym.integration.workload;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
public class ServiceJwtProvider {
    private final SecretKey signingKey;
    private final long expirationMs;

    public ServiceJwtProvider(@Value("${jwt.secret}") String secret,
                              @Value("${service.jwt.expiration-ms}") long expirationMs) {
        signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMs = expirationMs;
    }

    public String token() {
        Instant now = Instant.now();
        return Jwts.builder().subject("gym-crm-service")
                .issuer("gym-crm-service")
                .audience().add("gym-platform").and()
                .claim("scope", "workload.read.any workload.write")
                .issuedAt(Date.from(now)).expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(signingKey).compact();
    }
}
