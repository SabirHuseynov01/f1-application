package com.example.f1userservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            log.warn("Jwt token is empty");
            return false;
        }

        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Jwt expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Jwt not supported: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Jwt malformed: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("Jwt signature invalid: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Jwt token invalid: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("Jwt parsing failed: {}", e.getMessage());
        }

        return false;
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        String role = parseClaims(token).get("role", String.class);
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role claim is missing");
        }
        return role;
    }

    public Date getExpirationDate(String token) {
        return parseClaims(token).getExpiration();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
