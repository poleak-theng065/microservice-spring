package com.assigment.courseservice.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        logger.info("SecurityConfig initialized with JwtAuthenticationFilter");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.debug("Configuring security filter chain");

        try {
            SecurityFilterChain chain = http
                    .csrf(csrf -> {
                        csrf.disable();
                        logger.debug("CSRF protection disabled");
                    })
                    .authorizeHttpRequests(auth -> {
                        auth.requestMatchers("/auth/**").permitAll();
                        logger.debug("Public endpoints configured: /auth/**");
                        auth.anyRequest().authenticated();
                        logger.debug("All other endpoints require authentication");
                    })
                    .sessionManagement(sm -> {
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                        logger.debug("Session management set to STATELESS");
                    })
                    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                    .build();

            logger.info("Security filter chain configured successfully");
            return chain;

        } catch (Exception e) {
            logger.error("Failed to configure security filter chain", e);
            throw e;
        }
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        logger.debug("Creating AuthenticationManager bean");

        try {
            AuthenticationManager authManager = config.getAuthenticationManager();
            logger.info("AuthenticationManager created successfully");
            return authManager;

        } catch (Exception e) {
            logger.error("Failed to create AuthenticationManager", e);
            throw e;
        }
    }
}