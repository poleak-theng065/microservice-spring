package com.assigment.userservice.dto.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.awt.image.PixelGrabber;

@Data
@Accessors(chain = true)
@Getter
@Setter
public class LoginResponse {
    private int status;
    private String message;
    private TokenResponse token;
    private long expiresIn;

}