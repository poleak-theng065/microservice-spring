package com.assigment.userservice.controllers;

import com.assigment.userservice.dto.request.UserRequest;
import com.assigment.userservice.dto.response.UserResponse;
import com.assigment.userservice.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/user")
@RequiredArgsConstructor
public class UserController {

    private static final Logger logger = LogManager.getLogger(UserController.class);
    private final UserService service;

    /* ============================
       CREATE USER
       ============================ */
    @PostMapping("/")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserResponse> createUser(@RequestBody @Validated UserRequest request) {
        logger.info("Create user request received: {}", request.email());
        UserResponse response = service.createUser(request);
        logger.info("User created successfully: {}", response.getEmail());
        return ResponseEntity.ok(response);
    }

    /* ============================
       GET USERS
       ============================ */
    @GetMapping("/")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<?> findAll(Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            logger.info("Admin {} requested all users", authentication.getName());
            return ResponseEntity.ok(service.findAllUsers());
        } else {
            String currentUserEmail = authentication.getName();
            logger.info("User {} requested their own details", currentUserEmail);
            UserResponse user = service.FindByEmail(currentUserEmail);
            return ResponseEntity.ok(List.of(user));
        }
    }

    /* ============================
       CHECK USER EXISTENCE
       ============================ */
    @GetMapping("/exists/{user-id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Boolean> existsByUserId(@PathVariable("user-id") String userID) {
        logger.info("Check existence for userID: {}", userID);
        boolean exists = service.existsByID(userID);
        logger.info("UserID {} existence: {}", userID, exists);
        return ResponseEntity.ok(exists);
    }

    /* ============================
       GET USER BY ID
       ============================ */
    @GetMapping("/verification/{userID}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserResponse> findById(@PathVariable("userID") String userId) {
        logger.info("Admin requested user by ID: {}", userId);
        UserResponse user = service.FindById(userId);
        logger.info("Fetched user details: {}", user.getEmail());
        return ResponseEntity.ok(user);
    }

    /* ============================
       UPDATE USER
       ============================ */
    @PutMapping("/{userID}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateUser(
            @PathVariable String userID,
            @Valid @RequestBody UserRequest request
    ) {
        logger.info("Update request received for userID: {}", userID);

        request = new UserRequest(
                userID,
                request.firstName(),
                request.lastName(),
                request.phoneNumber(),
                request.email(),
                request.password(),
                request.role(),
                request.status()
        );

        service.updateUser(request);
        logger.info("User updated successfully: {}", request.email());
        return ResponseEntity.ok("User updated successfully");
    }

    /* ============================
       DELETE USER
       ============================ */
    @DeleteMapping("/{userID}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable("userID") String userID) {
        logger.info("Delete request received for userID: {}", userID);
        service.deleteCustomer(userID);
        logger.info("User deleted successfully: {}", userID);
        return ResponseEntity.ok().build();
    }
}
