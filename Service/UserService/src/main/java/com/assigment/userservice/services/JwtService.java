package com.assigment.userservice.services;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String generateAccessToken(UserDetails userDetails);

    String generateRefreshToken(UserDetails userDetails);

    long getExpirationTime();

    String extractUsername(String token);

    <T> T extractClaim(String token, java.util.function.Function<io.jsonwebtoken.Claims, T> claimsResolver);

    boolean isTokenValid(String token, UserDetails userDetails);
}
