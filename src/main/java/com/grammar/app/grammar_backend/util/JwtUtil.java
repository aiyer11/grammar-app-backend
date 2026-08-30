package com.grammar.app.grammar_backend.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;
    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    public String generateAccessToken(Long userId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        return createToken(claims, userId, jwtExpiration);
    }

    public String generateRefreshToken(Long userId) {
        return createToken(new HashMap<>(), userId, refreshExpiration);
    }

    public Long extractUserId(String token) {
        return Long.valueOf(extractClaim(token, Claims::getSubject));
    }

    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    public boolean isTokenExpired(String token) {
        try {
            extractAllClaims(token).getExpiration().before(new Date());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String createToken(Map<String, Object> claims, Long subject, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(subject))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    private <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
