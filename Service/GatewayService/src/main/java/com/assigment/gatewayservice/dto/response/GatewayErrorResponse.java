package com.assigment.gatewayservice.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayErrorResponse {
    private int status;
    private String message;
    private ErrorDetails details;
    private String path;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorDetails {
        private String error;
        private String message;
    }
}
