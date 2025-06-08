package com.application.security;

import java.security.Key;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenHelper {

    private final String SECRET_KEY = "thisIsASecretKeyForSpringBootApplication";
    private static Set<String> blacklistedTokens = new HashSet<>();
    
    public String generateToken(UserDetails userDetails, Long userId) {
        // Extract authorities and convert to a list of strings
        List<String> roles = userDetails.getAuthorities().stream()
            .map(auth -> auth.getAuthority())
            .toList();

        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .claim("userId", userId)
            .claim("roles", roles) // ✅ Add roles to the payload
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
            .signWith(getSignKey(), SignatureAlgorithm.HS256)
            .compact();
    }


    public boolean validateToken(String token, UserDetails userDetails) {
        if (isTokenBlacklisted(token)) {
            return false;
        }
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Claims claims = extractClaims(token);
        Object userIdClaim = claims.get("userId");
        return userIdClaim != null ? Long.parseLong(userIdClaim.toString()) : null;
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSignKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public static void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }

    public static boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}
