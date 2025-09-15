package com.assigment.userservice.services;

import com.assigment.userservice.dto.request.LoginUserDto;
import com.assigment.userservice.dto.request.RegisterUserDto;
import com.assigment.userservice.dto.response.LoginResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface AuthenticationService {

    // Signup / Verification
    void initiateSignup(RegisterUserDto dto) throws JsonProcessingException;
    boolean verifyAndSaveUser(String token) throws JsonProcessingException;

    // Login / Token
    LoginResponse login(LoginUserDto dto);
    String refreshAccessToken(String refreshToken);
    void logout(String userEmail);

    // Utilities
    boolean existsByEmail(String email);
    void sendResetLink(String email) throws JsonProcessingException;
    boolean resetPassword(String token, String newPassword);
}
