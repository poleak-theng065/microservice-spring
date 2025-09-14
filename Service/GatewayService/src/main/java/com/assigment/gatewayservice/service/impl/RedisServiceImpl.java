package com.assigment.gatewayservice.service.impl;

import com.assigment.gatewayservice.service.RedisService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisServiceImpl implements RedisService {

    private static final Logger logger = LogManager.getLogger(RedisServiceImpl.class);

    private final RedisTemplate<String, String> redisTemplate;

    public RedisServiceImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        logger.info("RedisServiceImpl initialized");
    }

    @Override
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
