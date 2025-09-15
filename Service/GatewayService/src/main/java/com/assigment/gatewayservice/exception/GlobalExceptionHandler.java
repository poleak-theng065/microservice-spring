package com.assigment.gatewayservice.exception;

import com.assigment.gatewayservice.dto.response.GatewayErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
@Component
@Order(-2)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        HttpStatus status;
        String message;

        if (ex instanceof WebExchangeBindException) { // Validation errors
            status = HttpStatus.BAD_REQUEST;
            message = "Validation failed";
        } else if (ex instanceof AuthenticationException) { // Auth failed
            status = HttpStatus.UNAUTHORIZED;
            message = "Authentication failed";
        } else if (ex instanceof AccessDeniedException) { // Authorization failed
            status = HttpStatus.FORBIDDEN;
            message = "Access denied";
        } else if (ex instanceof ResponseStatusException) { // HTTP specific
            ResponseStatusException rse = (ResponseStatusException) ex;
            status = (HttpStatus) rse.getStatusCode(); // <- use getStatusCode()
            message = rse.getReason() != null ? rse.getReason() : rse.getMessage();
        } else { // Internal error
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "Internal server error";
        }

        GatewayErrorResponse.ErrorDetails details = GatewayErrorResponse.ErrorDetails.builder()
                .error(ex.getClass().getSimpleName())
                .message(ex.getMessage())
                .build();

        GatewayErrorResponse errorResponse = GatewayErrorResponse.builder()
                .status(status.value())
                .message(message)
                .details(details)
                .path(exchange.getRequest().getPath().value())
                .build();

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(errorResponse);
        } catch (Exception e) {
            bytes = ("{\"status\":500,\"message\":\"Unexpected error\",\"details\":{\"error\":\"Exception\",\"message\":\"Unexpected error\"}}")
                    .getBytes();
        }

        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }
}
