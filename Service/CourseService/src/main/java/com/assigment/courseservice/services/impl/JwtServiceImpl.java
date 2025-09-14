package com.assigment.courseservice.services.impl;

import com.assigment.courseservice.services.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtServiceImpl implements JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtServiceImpl.class);

    @Value("${spring.security.jwt.secret-key}")
    private String secretKey;

    @Override
    public String extractRole(String token) {
        logger.debug("Extracting role from JWT token");
        try {
            String role = extractAllClaims(token).get("role", String.class);
            logger.debug("Successfully extracted role: {}", role);
            return role;
        } catch (Exception e) {
            logger.error("Failed to extract role from JWT token", e);
            throw e;
        }
    }

    @Override
    public boolean isTokenValid(String token) {
        logger.debug("Validating JWT token");
        try {
            Claims claims = extractAllClaims(token);
            boolean isValid = claims.getExpiration().after(new Date());

            if (isValid) {
                logger.debug("JWT token is valid");
            } else {
                logger.warn("JWT token has expired");
            }

            return isValid;
        } catch (Exception e) {
            logger.warn("JWT token validation failed: {}", e.getMessage());
            logger.debug("Token validation error details: ", e);
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        logger.trace("Extracting all claims from JWT token");
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            logger.trace("Successfully extracted {} claims from token", claims.size());
            return claims;

        } catch (Exception e) {
            logger.error("Failed to extract claims from JWT token", e);
            throw e;
        }
    }

    private Key getSignInKey() {
        logger.trace("Getting signing key for JWT validation");
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            Key key = Keys.hmacShaKeyFor(keyBytes);
            logger.trace("Signing key retrieved successfully");
            return key;
        } catch (Exception e) {
            logger.error("Failed to get signing key", e);
            throw e;
        }
    }
}
