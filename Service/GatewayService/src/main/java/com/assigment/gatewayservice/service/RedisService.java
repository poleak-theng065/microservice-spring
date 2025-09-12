package com.assigment.gatewayservice.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    private static final Logger logger = LogManager.getLogger(RedisService.class);

    private final RedisTemplate<String, String> redisTemplate;

    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        logger.info("RedisService initialized");
    }

    // Only get token by username
    public String getToken(String username) {
        String token = redisTemplate.opsForValue().get(username);
        if (token != null) {
            logger.info("Retrieved token from Redis for user '{}'", username);
        } else {
            logger.warn("No token found in Redis for user '{}'", username);
        }
        return token;
    }
}
