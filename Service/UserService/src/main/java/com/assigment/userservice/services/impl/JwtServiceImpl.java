package com.assigment.userservice.services.impl;

import com.assigment.userservice.entRepo.UserEntity;
import com.assigment.userservice.services.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {

    private static final Logger logger = LogManager.getLogger(JwtServiceImpl.class);

    @Value("${spring.security.jwt.secret-key}")
    private String secretKey;

    @Value("${spring.security.jwt.expiration-time}")
    private long jwtExpiration;

    @Value("${spring.security.jwt.refresh-expiration-time}")
    private long refreshExpirationTime;

    /* ============================
       TOKEN GENERATION METHODS
       ============================ */

    @Override
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof UserEntity user) {
            claims.put("role", user.getRole().name());
        }

        String token = buildToken(claims, userDetails, jwtExpiration);
        logger.info("Access token generated for user: {}", userDetails.getUsername());
        return token;
    }

    @Override
    public String generateRefreshToken(UserDetails userDetails) {
        String token = buildToken(new HashMap<>(), userDetails, refreshExpirationTime);
        logger.info("Refresh token generated for user: {}", userDetails.getUsername());
        return token;
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public long getExpirationTime() {
        return jwtExpiration;
    }

    /* ============================
       TOKEN EXTRACTION METHODS
       ============================ */

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            logger.error("Failed to extract claims from token: {}", e.getMessage(), e);
            throw e;
        }
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /* ============================
       TOKEN VALIDATION METHODS
       ============================ */

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        boolean valid = extractUsername(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
        if (!valid) {
            logger.warn("Invalid or expired token for user: {}", userDetails.getUsername());
        }
        return valid;
    }

    private boolean isTokenExpired(String token) {
        boolean expired = extractExpiration(token).before(new Date());
        if (expired) {
            logger.warn("Token expired: {}", token);
        }
        return expired;
    }

    /* ============================
       HELPER METHODS
       ============================ */

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
