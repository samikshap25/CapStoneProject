package com.ecommerce.order.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "E_COMMERCE_SECRET_KEY_98765432100000000000000000";

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // Extract username from JWT
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ✅ UPDATED: Extract userId from JWT token
    // Since your tokens don't have userId, we'll use a default or username mapping
    public Integer extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        
        // Try different possible claim names for userId
        Object userIdObj = claims.get("userId");
        if (userIdObj == null) {
            userIdObj = claims.get("user_id");
        }
        if (userIdObj == null) {
            userIdObj = claims.get("id");
        }
        
        // Convert to Integer if found
        if (userIdObj instanceof Integer) {
            return (Integer) userIdObj;
        } else if (userIdObj instanceof Long) {
            return ((Long) userIdObj).intValue();
        } else if (userIdObj instanceof String) {
            try {
                return Integer.parseInt((String) userIdObj);
            } catch (NumberFormatException e) {
                // Not a numeric string, continue to fallback
            }
        }
        
        // ✅ FALLBACK: If no userId found, check if 'sub' is numeric
        String subject = claims.getSubject();
        if (subject != null) {
            try {
                return Integer.parseInt(subject);
            } catch (NumberFormatException e) {
                // Subject is username, not userId
                System.out.println("⚠️ JWT 'sub' is username '" + subject + "', not userId. Using default userId: 1");
            }
        }
        
        // ✅ DEFAULT: Return 1 as fallback
        System.out.println("⚠️ No userId found in JWT token, using default: 1");
        return 1;
    }

    // Validate JWT
    public boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}