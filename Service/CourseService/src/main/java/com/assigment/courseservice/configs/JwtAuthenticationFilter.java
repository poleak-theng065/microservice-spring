package com.assigment.courseservice.configs;

import com.assigment.courseservice.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
        logger.debug("JwtAuthenticationFilter initialized");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        logger.debug("Processing request: {} {}", request.getMethod(), request.getRequestURI());

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("No Bearer token found in Authorization header");
            filterChain.doFilter(request, response);
            return;
        }

        logger.debug("Bearer token found in Authorization header");
        final String jwt = authHeader.substring(7);
        logger.trace("JWT token extracted: {}", jwt.substring(0, Math.min(20, jwt.length())) + "...");

        if (!jwtService.isTokenValid(jwt)) {
            logger.warn("Invalid JWT token received");
            filterChain.doFilter(request, response);
            return;
        }

        logger.debug("JWT token is valid");
        final String role = jwtService.extractRole(jwt);
        logger.debug("Extracted role from JWT: {}", role);

        if (role != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.debug("Creating authentication token for role: {}", role);

            // No username, only role
            User userDetails = new User(
                    "anonymous", // placeholder
                    "",
                    Collections.singleton(() -> "ROLE_" + role.toUpperCase())
            );

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            logger.info("Authentication successful for role: {}", role);
            logger.debug("Security context updated with authentication");
        } else {
            logger.debug("No authentication context update needed");
        }

        logger.debug("Continuing filter chain");
        filterChain.doFilter(request, response);
    }
}