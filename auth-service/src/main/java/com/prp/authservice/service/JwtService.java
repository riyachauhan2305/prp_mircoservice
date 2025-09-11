package com.prp.authservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.prp.commonconfig.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;



@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access.expiry}")
    private long accessExpiry;

    @Value("${jwt.refresh.expiry}")
    private long refreshExpiry;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

   

    private String buildToken(Map<String, Object> claims, String subject, long expiryMillis) {
    long nowMillis = System.currentTimeMillis();
    Date now = new Date(nowMillis);
    Date expiryDate = new Date(nowMillis + expiryMillis);

    return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)   // <-- Add this
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
}


    public String generateSessionToken(String sessionToken, long expiryMillis) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sessionToken", sessionToken);
        claims.put("type", "session");
        return buildToken(claims, sessionToken, expiryMillis);
    }

    public String generateAccessToken(String userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "access");
        return buildToken(claims, userId, accessExpiry);
    }


     public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String generateRefreshToken(String userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh");
        return buildToken(claims, userId, refreshExpiry);
    }

   


public String generateUserToken(String userId, long expiryMillis) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("id", userId);       // This will match User.id
    claims.put("type", "user");
    return buildToken(claims, userId, expiryMillis);
}



    // ------------------ Token Extraction ------------------

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getTokenType(String token) {
        Object type = extractAllClaims(token).get("type");
        return type != null ? type.toString() : null;
    }

   

    public static String extractUserId(HttpServletRequest request, JwtService jwtService) {
    String token = extractToken(request);
    if (token == null || !jwtService.isTokenValid(token)) return null;

    Claims claims = jwtService.extractAllClaims(token);
    Object userIdClaim = claims.get("userId"); // <-- use standardized claim
    if (userIdClaim != null) return userIdClaim.toString();

    return null;
}

    // ------------------ Token Validation ------------------

    public boolean isTokenValid(String token) {
        try {
            return !extractAllClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSessionToken(String token) {
        return "session".equalsIgnoreCase(getTokenType(token));
    }

    public boolean isUserToken(String token) {
        return "user".equalsIgnoreCase(getTokenType(token));
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equalsIgnoreCase(getTokenType(token));
    }

    public long getTokenExpiry(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }

    public static String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return null;
        return header.substring(7);
    }

    public static String extractSessionToken(HttpServletRequest request, JwtService jwtService) {
        String token = extractToken(request);

        log.info("Extracted token from request: {}", token);

        if (token == null || !jwtService.isTokenValid(token)) return null;

        String tokenType = jwtService.getTokenType(token);
        if (!"session".equalsIgnoreCase(tokenType) && !"access".equalsIgnoreCase(tokenType)) return null;

        Claims claims = jwtService.extractAllClaims(token);
        String sessionToken = claims.get("sessionToken", String.class);

        log.info("Extracted sessionToken from claims: {}", sessionToken);

        return sessionToken;
    }

    public static String extractUserIdFromToken(HttpServletRequest request, JwtService jwtService) {
        String token = extractToken(request);
        if (token == null || !jwtService.isTokenValid(token)) return null;

        String tokenType = jwtService.getTokenType(token);
        if ("refresh".equalsIgnoreCase(tokenType) || "user".equalsIgnoreCase(tokenType)) {
            Claims claims = jwtService.extractAllClaims(token);
            return claims.get("id", String.class);
        }

        return null;
    }

    public String getUserId(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            throw new UnauthorizedException("Unauthorized: Missing or invalid token");
        }
        return userId;
    }
}
