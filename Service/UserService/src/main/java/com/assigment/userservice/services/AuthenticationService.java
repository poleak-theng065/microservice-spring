package com.assigment.userservice.services;

import com.assigment.userservice.dto.request.LoginUserDto;
import com.assigment.userservice.dto.request.RegisterUserDto;
import com.assigment.userservice.dto.response.*;
import com.assigment.userservice.entRepo.UserEntity;
import com.assigment.userservice.entRepo.UserRepository;
import com.assigment.userservice.constants.RoleEnum;
import com.assigment.userservice.constants.Status;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AuthenticationService {

    private static final Logger logger = LogManager.getLogger(AuthenticationService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;

    public AuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper,
            RabbitTemplate rabbitTemplate
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.rabbitTemplate = rabbitTemplate;

        logger.info("AuthenticationService initialized");
    }

    /* ============================
       SIGNUP / VERIFICATION
       ============================ */
    public void initiateSignup(RegisterUserDto dto) throws JsonProcessingException {
        String verificationToken = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set(
                "SIGNUP:" + verificationToken,
                objectMapper.writeValueAsString(dto),
                15, TimeUnit.MINUTES
        );

        rabbitTemplate.convertAndSend(
                "mail.exchange",
                "mail.signup",
                new VerificationMessage(dto.getEmail(), verificationToken)
        );

        logger.info("Signup initiated for email: {}, token stored in Redis", dto.getEmail());
    }

    public boolean verifyAndSaveUser(String token) throws JsonProcessingException {
        String key = "SIGNUP:" + token;
        String userJson = redisTemplate.opsForValue().get(key);

        if (userJson == null) {
            logger.warn("Verification failed: token not found in Redis: {}", token);
            return false;
        }

        RegisterUserDto dto = objectMapper.readValue(userJson, RegisterUserDto.class);

        UserEntity user = new UserEntity()
                .setFirstName(dto.getFirstName())
                .setLastName(dto.getLastName())
                .setPhoneNumber(dto.getPhoneNumber())
                .setEmail(dto.getEmail())
                .setPassword(passwordEncoder.encode(dto.getPassword()))
                .setRole(RoleEnum.USER)
                .setStatus(Status.ENABLE);

        userRepository.save(user);
        redisTemplate.delete(key);

        logger.info("User verified and saved: {}", dto.getEmail());
        return true;
    }

    /* ============================
       LOGIN / TOKEN MANAGEMENT
       ============================ */
    public LoginResponse login(LoginUserDto dto) {
        logger.info("Login attempt for email: {}", dto.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );
        UserEntity user = (UserEntity) authentication.getPrincipal();

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        redisTemplate.opsForValue().set("REFRESH:" + refreshToken, user.getUsername(), 7, TimeUnit.DAYS);

        logger.info("Login successful, tokens generated for email: {}", dto.getEmail());

        TokenResponse tokenResponse = new TokenResponse()
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken);

        UserResponse userResponse = UserResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .build();

        return new LoginResponse()
                .setToken(tokenResponse)
                .setUser(userResponse)
                .setExpiresIn(jwtService.getExpirationTime());
    }

    public String refreshAccessToken(String refreshToken) {
        String username = redisTemplate.opsForValue().get("REFRESH:" + refreshToken);

        if (username == null) {
            logger.warn("Refresh token invalid or expired: {}", refreshToken);
            throw new RuntimeException("Invalid refresh token");
        }

        UserEntity user = userRepository.findByEmail(username).orElseThrow();
        String newAccessToken = jwtService.generateAccessToken(user);

        logger.info("Access token refreshed for user: {}", username);
        return newAccessToken;
    }

    public void logout(String refreshToken) {
        if (refreshToken != null) {
            redisTemplate.delete("REFRESH:" + refreshToken);
            logger.info("User logged out, refresh token deleted: {}", refreshToken);
        }
    }

    /* ============================
       UTILITIES / MISC
       ============================ */
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public void sendResetLink(String email) throws JsonProcessingException {
        String resetToken = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set("RESET_TOKEN:" + resetToken, email, 10, TimeUnit.MINUTES);

        rabbitTemplate.convertAndSend(
                "mail.exchange",
                "mail.reset",
                new ResetMessage(email, resetToken)
        );

        logger.info("Password reset link sent to email: {}, token stored in Redis", email);
    }

    public boolean resetPassword(String token, String newPassword) {
        String email = redisTemplate.opsForValue().get("RESET_TOKEN:" + token);
        if (email == null) {
            logger.warn("Reset password failed: invalid or expired token: {}", token);
            return false;
        }

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        redisTemplate.delete("RESET_TOKEN:" + token);

        logger.info("Password reset successfully for email: {}", email);
        return true;
    }

}
