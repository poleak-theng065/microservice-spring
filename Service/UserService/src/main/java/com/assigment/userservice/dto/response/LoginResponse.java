package com.assigment.userservice.dto.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Getter
@Setter
public class LoginResponse {
    private TokenResponse token;
    private UserResponse user;  // reuse your existing user response DTO
    private long expiresIn;
}