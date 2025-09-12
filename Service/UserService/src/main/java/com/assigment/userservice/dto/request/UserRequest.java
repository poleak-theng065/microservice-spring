package com.assigment.userservice.dto.request;

import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record UserRequest(
         String userID,
         @NotNull(message = "User first name are require!")
         String firstName,
         @NotNull(message = "User last name are require!")
         String lastName,
         @NotNull(message = "User phone number are require!")
         String phoneNumber,
         @NotNull(message = "User email are require!")
         @Email(message = "Customer email is not a valid email address")
         String email,
         @NotNull
         String password,
         @NotNull
         String role,
         @NotNull
         String status
) {
}
