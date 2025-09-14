package com.assigment.courseservice.services;

public interface JwtService {
    String extractRole(String token);
    boolean isTokenValid(String token);
}
