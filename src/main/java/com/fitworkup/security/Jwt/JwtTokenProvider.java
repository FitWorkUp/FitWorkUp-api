package com.fitworkup.security.Jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms:86400000}")
    private Long jwtExpirationInMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = this.jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 64) {
            logger.warn("ALERTA DE SEGURANÇA: A chave 'app.jwt.secret' possui menos de 64 bytes (512 bits). Recomendado aumentar para prevenir ataques contra HS512.");
        }
        return Keys.hmacShaKeyFor(keyBytes);
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
        } catch (SignatureException ex) {
            logger.error("Assinatura do JWT inválida ou adulterada: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Token JWT malformatado: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.warn("Token JWT expirado: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Token JWT não suportado: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("Claims do JWT estão vazias ou nulas: {}", ex.getMessage());
        }
        return false;
    }
}