package com.assigment.userservice.services;

import com.assigment.userservice.dto.request.LoginUserDto;
import com.assigment.userservice.dto.request.RegisterUserDto;
import com.assigment.userservice.dto.response.LoginResponse;
import com.assigment.userservice.dto.response.RefreshTokenResponse;
import com.assigment.userservice.dto.response.UserStandardResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface AuthenticationService {

    // Signup / Verification
    void initiateSignup(RegisterUserDto dto) throws JsonProcessingException;
    boolean verifyAndSaveUser(String token) throws JsonProcessingException;

    // Login / Token
    LoginResponse login(LoginUserDto dto);
    RefreshTokenResponse refreshAccessToken(String refreshToken);
    UserStandardResponse logout(String userEmail);

    // Utilities
    boolean existsByEmail(String email);
    void sendResetLink(String email) throws JsonProcessingException;
    boolean resetPassword(String token, String newPassword);
}
