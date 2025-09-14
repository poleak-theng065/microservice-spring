package com.assigment.gatewayservice.service;

public interface JwtService {

    boolean isTokenValid(String token);

    String extractUsername(String token);

    String extractRole(String token);
}
