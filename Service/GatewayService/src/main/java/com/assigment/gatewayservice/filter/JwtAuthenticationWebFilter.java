package com.assigment.gatewayservice.filter;

import com.assigment.gatewayservice.service.JwtService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@Component
public class JwtAuthenticationWebFilter implements WebFilter {

    private static final Logger logger = LogManager.getLogger(JwtAuthenticationWebFilter.class);

    private final JwtService jwtService;
    private final RedisTemplate<String, String> redisTemplate;

    public JwtAuthenticationWebFilter(JwtService jwtService, RedisTemplate<String, String> redisTemplate) {
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("No Authorization header found or header invalid for request: {}", exchange.getRequest().getPath());
            return chain.filter(exchange); // no token
        }

        String accessToken = authHeader.substring(7);

        // Validate Access Token signature and expiration
        if (!jwtService.isTokenValid(accessToken)) {
            logger.warn("Invalid or expired access token for request: {}", exchange.getRequest().getPath());
            return chain.filter(exchange); // invalid token
        }

        // Extract username and role from Access Token
        String usernameFromAccessToken = jwtService.extractUsername(accessToken);
        String roleFromAccessToken = jwtService.extractRole(accessToken);

        // Check Redis for any REFRESH token with this username as value
        Set<String> refreshKeys = redisTemplate.keys("REFRESH:*");
        boolean valid = false;

        for (String key : refreshKeys) {
            String usernameInRedis = redisTemplate.opsForValue().get(key);
            if (usernameFromAccessToken.equals(usernameInRedis)) {
                valid = true;
                break;
            }
        }

        if (!valid) {
            logger.warn("No valid refresh token found in Redis for user: {}", usernameFromAccessToken);
            return chain.filter(exchange); // no valid refresh token found
        }

        // Build Authorities from role
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + roleFromAccessToken));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(usernameFromAccessToken, null, authorities);

        SecurityContext context = new SecurityContextImpl(authentication);

        logger.info("Authenticated user '{}' with role '{}' for request: {}", usernameFromAccessToken, roleFromAccessToken, exchange.getRequest().getPath());

        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
    }
}
