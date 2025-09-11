package com.prp.transactionservice.util;

import com.prp.transactionservice.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public String extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7); // remove "Bearer " prefix

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Use "id" from the token payload
            String userId = claims.get("id", String.class);
            if (userId == null) {
                throw new UnauthorizedException("id not found in token");
            }

            return userId;
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid or expired JWT token");
        }
    }
}
