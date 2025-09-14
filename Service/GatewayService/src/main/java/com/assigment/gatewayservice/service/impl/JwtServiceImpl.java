package com.assigment.gatewayservice.service.impl;

import com.assigment.gatewayservice.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtServiceImpl implements JwtService {

    private static final Logger logger = LogManager.getLogger(JwtServiceImpl.class);

    @Value("${spring.security.jwt.secret-key}")
    private String secretKey;

    @Override
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            boolean valid = !isTokenExpired(claims);
            logger.info("Token validation success for user: {}", claims.getSubject());
            return valid;
        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String extractUsername(String token) {
        String username = extractAllClaims(token).getSubject();
        logger.info("Extracted username from token: {}", username);
        return username;
    }

    @Override
    public String extractRole(String token) {
        String role = extractAllClaims(token).get("role", String.class);
        logger.info("Extracted role from token: {}", role);
        return role;
    }

    /* ============================
       PRIVATE HELPERS
       ============================ */

    private Claims extractAllClaims(String token) {
        Key key = getSignInKey();
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(Claims claims) {
        boolean expired = claims.getExpiration().before(new Date());
        if (expired) {
            logger.warn("Token expired for user: {}", claims.getSubject());
        }
        return expired;
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
