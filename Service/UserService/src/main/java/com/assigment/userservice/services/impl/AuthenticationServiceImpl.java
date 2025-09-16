package com.assigment.userservice.services.impl;

import com.assigment.userservice.configs.RabbitConfig;
import com.assigment.userservice.dto.request.LoginUserDto;
import com.assigment.userservice.dto.request.RegisterUserDto;
import com.assigment.userservice.dto.response.*;
import com.assigment.userservice.entRepo.UserEntity;
import com.assigment.userservice.entRepo.UserRepository;
import com.assigment.userservice.constants.RoleEnum;
import com.assigment.userservice.constants.StatusEnum;
import com.assigment.userservice.exceptions.DuplicateResourceException;
import com.assigment.userservice.services.AuthenticationService;
import com.assigment.userservice.services.JwtService;
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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Logger logger = LogManager.getLogger(AuthenticationServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;

    public AuthenticationServiceImpl(
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

        logger.info("AuthenticationServiceImpl initialized");
    }

    /* ============================
       SIGNUP / VERIFICATION
       ============================ */
    @Override
    public void initiateSignup(RegisterUserDto dto) throws JsonProcessingException {
        String verificationToken = UUID.randomUUID().toString();

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email already exists");
        } else if (userRepository.findByPhoneNumber(dto.getPhoneNumber()).isPresent()) {
            throw new DuplicateResourceException("Phone number already exists");
        }

        redisTemplate.opsForValue().set(
                "SIGNUP:" + verificationToken,
                objectMapper.writeValueAsString(dto),
                15, TimeUnit.MINUTES
        );

        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                RabbitConfig.VERIFICATION_ROUTING_KEY,
                new VerificationMessage(dto.getEmail(), verificationToken)
        );

        logger.info("Signup initiated for email: {}, token stored in Redis", dto.getEmail());
    }

    @Override
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
                .setStatus(StatusEnum.ENABLE);

        userRepository.save(user);
        redisTemplate.delete(key);

        logger.info("User verified and saved: {}", dto.getEmail());
        return true;
    }

    /* ============================
       LOGIN / TOKEN MANAGEMENT
       ============================ */
    @Override
    public LoginResponse login(LoginUserDto dto) {
        logger.info("Login attempt for email: {}", dto.getEmail());

        try {
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

            return new LoginResponse()
                    .setStatus(200)
                    .setMessage("Login successful!")
                    .setToken(tokenResponse)
                    .setExpiresIn(jwtService.getExpirationTime());

        } catch (Exception ex) {
            logger.warn("Login failed for email: {}", dto.getEmail());
            return new LoginResponse()
                    .setStatus(401)
                    .setMessage("Invalid email or password");
        }
    }

    @Override
    public RefreshTokenResponse refreshAccessToken(String refreshToken) {
        String username = redisTemplate.opsForValue().get("REFRESH:" + refreshToken);

        if (username == null) {
            logger.warn("Refresh token invalid or expired: {}", refreshToken);
            return RefreshTokenResponse.builder()
                    .status(401)
                    .message("Invalid or expired refresh token")
                    .accessToken(null)
                    .build();
        }

        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtService.generateAccessToken(user);
        logger.info("Access token refreshed for user: {}", username);

        return RefreshTokenResponse.builder()
                .status(200)
                .message("Access token refreshed successfully")
                .accessToken(newAccessToken)
                .build();
    }

    @Override
    public UserStandardResponse logout(String userEmail) {
        boolean tokenDeleted = false;
        Set<String> keys = redisTemplate.keys("REFRESH:*");

        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                String value = redisTemplate.opsForValue().get(key);
                if (userEmail.equals(value)) {
                    redisTemplate.delete(key);
                    logger.info("Deleted refresh token {} for user: {}", key, userEmail);
                    tokenDeleted = true;
                    break;
                }
            }
        }

        if (!tokenDeleted) {
            logger.info("No refresh tokens found for user: {}", userEmail);
        }

        return UserStandardResponse.builder()
                .status(200)
                .message("Logged out successfully")
                .users(List.of())
                .build();
    }

    /* ============================
       UTILITIES / MISC
       ============================ */
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public void sendResetLink(String email) throws JsonProcessingException {
        String resetToken = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set("RESET_TOKEN:" + resetToken, email, 10, TimeUnit.MINUTES);

        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                RabbitConfig.RESET_ROUTING_KEY,
                new ResetMessage(email, resetToken)
        );

        logger.info("Password reset link sent to email: {}, token stored in Redis", email);
    }

    @Override
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
