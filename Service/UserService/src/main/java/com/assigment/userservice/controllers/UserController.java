package com.assigment.userservice.controllers;

import com.assigment.userservice.dto.request.UserRequest;
import com.assigment.userservice.dto.response.PaginatedUserResponse;
import com.assigment.userservice.dto.response.UserStandardResponse;
import com.assigment.userservice.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/user")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService service;

    /* CREATE USER (admin only) */
    @PostMapping("/")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserStandardResponse> createUser(@RequestBody @Valid UserRequest request) {
        return ResponseEntity.ok(service.createUser(request));
    }

    /* GET USERS (based on role)
       pagination: ?page=0&size=5  (defaults page=0 size=5)
    */
    @GetMapping("/")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<UserStandardResponse> findAll(
            Authentication authentication,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(service.findAllUsersBasedOnRole(authentication, page, size));
    }

    /* CHECK USER EXISTENCE */
    @GetMapping("/exists/{user-id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserStandardResponse> existsByUserId(@PathVariable("user-id") String userID) {
        return ResponseEntity.ok(service.existsByID(userID));
    }

    /* GET USER BY ID (admin only) */
    @GetMapping("/verification/{userID}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserStandardResponse> findById(@PathVariable("userID") String userId) {
        return ResponseEntity.ok(service.findById(userId));
    }

    /* UPDATE USER (admin only) */
    @PutMapping("/{userID}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserStandardResponse> updateUser(
            @PathVariable String userID,
            @Valid @RequestBody UserRequest request
    ) {
        return ResponseEntity.ok(service.updateUser(userID, request));
    }

    /* DELETE USER (admin only) */
    @DeleteMapping("/{userID}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity delete(@PathVariable("userID") String userID) {
        return ResponseEntity.ok(service.deleteCustomer(userID));
    }

    /* PATCH user status: enable or disable (admin only)
       Example: PATCH /api/user/{id}/status?enable=true
    */
    @PatchMapping("/{userID}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserStandardResponse> updateUserStatus(
            @PathVariable("userID") String userID,
            @RequestParam("enable") boolean enable
    ) {
        return ResponseEntity.ok(service.updateUserStatus(userID, enable));
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<PaginatedUserResponse> getUsersPaginated(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            HttpServletRequest request
    ) {
        String baseUrl = request.getRequestURL().toString() + "?size=" + size;
        PaginatedUserResponse response = service.findAllUsersPaginated(page, size, baseUrl);
        return ResponseEntity.ok(response);
    }

}
