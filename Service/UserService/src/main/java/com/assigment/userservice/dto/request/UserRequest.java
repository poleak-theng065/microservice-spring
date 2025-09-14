package com.assigment.userservice.dto.request;

import com.assigment.userservice.constants.RoleEnum;
import com.assigment.userservice.constants.StatusEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @Email(message = "Must be a valid email")
    @NotBlank(message = "Email is required")
    private String email;

    // Password is optional for updates; required for create (validate in controller/service if needed)
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private RoleEnum role;
    private StatusEnum status;
}

