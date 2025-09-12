package com.assigment.userservice.dto.response;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
}