package com.fitworkup.security.Jwt;

import jakarta.annotation.PostConstruct;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final int HS512_MINIMUM_SECRET_BYTES = 64;

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms:86400000}")
    private Long jwtExpirationInMs;

    private SecretKey signingKey;

    @PostConstruct
    void initializeSigningKey() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("JWT secret must be configured.");
        }

        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < HS512_MINIMUM_SECRET_BYTES) {
            throw new IllegalStateException("JWT secret must contain at least 64 bytes for HS512.");
        }
        if (jwtExpirationInMs == null || jwtExpirationInMs <= 0) {
            throw new IllegalStateException("JWT expiration must be greater than zero.");
        }

        signingKey = Keys.hmacShaKeyFor(keyBytes);
        logger.info("JWT signing configuration initialized with HS512.");
    }

    private SecretKey getSigningKey() {
        if (signingKey == null) {
            throw new IllegalStateException("JWT signing key has not been initialized.");
        }
        return signingKey;
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    public long getExpirationInMs() {
        return jwtExpirationInMs;
    }

    public String getUsernameFromJWT(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(authToken);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException ex) {
            logger.warn("JWT validation rejected: reason=invalid_signature");
        } catch (MalformedJwtException ex) {
            logger.warn("JWT validation rejected: reason=malformed_token");
        } catch (ExpiredJwtException ex) {
            logger.debug("JWT validation rejected: reason=expired_token");
        } catch (UnsupportedJwtException ex) {
            logger.warn("JWT validation rejected: reason=unsupported_token");
        } catch (IllegalArgumentException ex) {
            logger.warn("JWT validation rejected: reason=empty_or_invalid_claims");
        }
        return false;
    }
}