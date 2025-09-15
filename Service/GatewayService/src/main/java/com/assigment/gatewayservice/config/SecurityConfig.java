package com.assigment.gatewayservice.config;

import com.assigment.gatewayservice.Security.CustomAccessDeniedHandler;
import com.assigment.gatewayservice.Security.CustomAuthenticationEntryPoint;
import com.assigment.gatewayservice.filter.JwtAuthenticationWebFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final Logger logger = LogManager.getLogger(SecurityConfig.class);

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            JwtAuthenticationWebFilter jwtFilter,
            CustomAuthenticationEntryPoint authEntryPoint,
            CustomAccessDeniedHandler accessDeniedHandler) {

        logger.info("Initializing SecurityWebFilterChain with JWT filter at AUTHENTICATION order");

        SecurityWebFilterChain chain = http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/auth/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/user").hasAnyRole("USER", "ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/user").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/user/{userID}").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/api/users/verification/{userID}").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/user/{userID}").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/course/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/course/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/course/{id}/status").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/course/all").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/course/{id}").hasAnyRole("ADMIN", "USER")
                        .pathMatchers(HttpMethod.GET, "/course").hasAnyRole("USER", "ADMIN")
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();

        logger.info("SecurityWebFilterChain configured successfully");

        return chain;
    }
}
