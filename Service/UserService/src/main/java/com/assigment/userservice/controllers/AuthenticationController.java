package com.assigment.userservice.controllers;

import com.assigment.userservice.dto.request.LoginUserDto;
import com.assigment.userservice.dto.request.RegisterUserDto;
import com.assigment.userservice.dto.response.LoginResponse;
import com.assigment.userservice.services.AuthenticationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/auth")
@RestController
public class AuthenticationController {

    private static final Logger logger = LogManager.getLogger(AuthenticationController.class);
    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody RegisterUserDto dto) throws JsonProcessingException {
        logger.info("Signup request received for email: {}", dto.getEmail());
        authenticationService.initiateSignup(dto);
        logger.info("Verification email sent to: {}", dto.getEmail());
        return ResponseEntity.ok("Verification email sent. Please check your inbox.");
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam("token") String token) throws JsonProcessingException {
        logger.info("Verification attempt with token: {}", token);
        boolean isVerified = authenticationService.verifyAndSaveUser(token);
        if (isVerified) {
            logger.info("Account verified successfully for token: {}", token);
            return ResponseEntity.ok("Account verified successfully!");
        }
        logger.warn("Verification failed or expired for token: {}", token);
        return ResponseEntity.badRequest().body("Verification link expired or invalid");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@Valid @RequestBody LoginUserDto loginUserDto) {
        logger.info("Login attempt for email: {}", loginUserDto.getEmail());
        LoginResponse loginResponse = authenticationService.login(loginUserDto);
        logger.info("Login successful for email: {}", loginUserDto.getEmail());
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        logger.info("Refreshing access token for refreshToken: {}", refreshToken);
        String newAccessToken = authenticationService.refreshAccessToken(refreshToken);
        logger.info("New access token generated successfully");
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User is not authenticated");
        }

        String userEmail = authentication.getName();
        authenticationService.logout(userEmail);

        return ResponseEntity.ok("Logged out successfully");
    }



    @PostMapping("/reset")
    public ResponseEntity<String> reset(@RequestBody Map<String, String> body) throws JsonProcessingException {
        String email = body.get("email");
        logger.info("Password reset requested for email: {}", email);
        boolean emailExists = authenticationService.existsByEmail(email);
        if (emailExists) {
            authenticationService.sendResetLink(email);
            logger.info("Reset link sent to email: {}", email);
            return ResponseEntity.ok("Reset link sent. Please check your inbox.");
        }
        logger.warn("Password reset failed, email not found: {}", email);
        return ResponseEntity.badRequest().body("Email not found");
    }

    @PostMapping("/verification/reset")
    public ResponseEntity<?> resetToken(
            @RequestParam("token") String token,
            @RequestBody Map<String, String> body) {

        String newPassword = body.get("newPassword");
        logger.info("Reset password attempt for token: {}", token);

        boolean success = authenticationService.resetPassword(token, newPassword);
        if (success) {
            logger.info("Password reset successfully for token: {}", token);
            return ResponseEntity.ok("Password has been reset successfully!");
        } else {
            logger.warn("Password reset failed or token expired for token: {}", token);
            return ResponseEntity.badRequest().body("Invalid or expired token");
        }
    }
}
