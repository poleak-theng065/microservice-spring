package com.assigment.userservice.services.impl;

import com.assigment.userservice.constants.RoleEnum;
import com.assigment.userservice.constants.StatusEnum;
import com.assigment.userservice.dto.mapper.UserMapper;
import com.assigment.userservice.dto.request.UserRequest;
import com.assigment.userservice.dto.response.PaginatedUserResponse;
import com.assigment.userservice.dto.response.UserResponse;
import com.assigment.userservice.dto.response.UserStandardResponse;
import com.assigment.userservice.entRepo.UserEntity;
import com.assigment.userservice.entRepo.UserRepository;
import com.assigment.userservice.exceptions.UserNotFoundException;
import com.assigment.userservice.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);

    private final UserRepository repository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    /* ============================================================
       CREATE / UPDATE METHODS
    ============================================================ */

    @Override
    public UserStandardResponse createUser(@Valid UserRequest request) {
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return UserStandardResponse.builder()
                    .status(400)
                    .message("Password is required when creating a user")
                    .users(List.of())
                    .build();
        }

        UserEntity toSave = mapper.toUser(request);
        toSave.setPassword(passwordEncoder.encode(request.getPassword()));

        if (toSave.getRole() == null) {
            toSave.setRole(RoleEnum.USER);
        }
        if (toSave.getStatus() == null) {
            toSave.setStatus(StatusEnum.ENABLE);
        }

        var user = repository.save(toSave);
        logger.info("Created new user with ID: {}", user.getUserID());

        return UserStandardResponse.builder()
                .status(200)
                .message("User created successfully")
                .users(List.of(mapper.fromUser(user)))
                .build();
    }

    @Override
    public UserStandardResponse updateUser(String userID, @Valid UserRequest request) {
        var user = repository.findById(userID)
                .orElseThrow(() -> new UserNotFoundException(
                        format("Cannot update user:: No user found with ID:: %s", userID)
                ));

        mergeUser(user, request);
        var savedUser = repository.save(user);

        logger.info("Updated user with ID: {}", userID);

        return UserStandardResponse.builder()
                .status(200)
                .message("User updated successfully")
                .users(List.of(mapper.fromUser(savedUser)))
                .build();
    }

    private void mergeUser(UserEntity user, UserRequest request) {
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
    }

    /* ============================================================
       READ METHODS
    ============================================================ */

    public UserStandardResponse findAllUsers(int page, int size) {
        if (size <= 0) size = 5;
        if (page < 0) page = 0;

        Page<UserEntity> result = repository.findAll(PageRequest.of(page, size));
        List<UserResponse> users = result.stream().map(mapper::fromUser).collect(Collectors.toList());

        if (users.isEmpty()) {
            return UserStandardResponse.builder()
                    .status(404)
                    .message("No users found")
                    .users(users)
                    .build();
        }

        return UserStandardResponse.builder()
                .status(200)
                .message("Users fetched successfully")
                .users(users)
                .build();
    }

    @Override
    public UserStandardResponse findById(String userId) {
        var user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(format("Cannot find user:: %s", userId)));

        return UserStandardResponse.builder()
                .status(200)
                .message("User fetched successfully")
                .users(List.of(mapper.fromUser(user)))
                .build();
    }

    @Override
    public UserStandardResponse findAllUsersBasedOnRole(Authentication authentication, int page, int size) {
        String currentUserEmail = authentication.getName();
        logger.info("User {} requested their own details", currentUserEmail);

        var user = repository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + currentUserEmail));

        return UserStandardResponse.builder()
                .status(200)
                .message("User fetched successfully")
                .users(List.of(mapper.fromUser(user)))
                .build();
    }

    public PaginatedUserResponse findAllUsersPaginated(int page, int size, String baseUrl) {
        if (size <= 0) size = 5;
        if (page < 0) page = 0;

        Page<UserEntity> result = repository.findAll(PageRequest.of(page, size));
        List<UserResponse> users = result.getContent().stream().map(mapper::fromUser).toList();

        String nextPageUrl = result.hasNext() ? baseUrl + "&page=" + (page + 1) : null;
        String previousPageUrl = result.hasPrevious() ? baseUrl + "&page=" + (page - 1) : null;

        return PaginatedUserResponse.builder()
                .status(users.isEmpty() ? 404 : 200)
                .message(users.isEmpty() ? "No users found" : "Users fetched successfully")
                .users(users)
                .currentPage(result.getNumber())
                .totalPages(result.getTotalPages())
                .totalElements(result.getTotalElements())
                .hasNext(result.hasNext())
                .hasPrevious(result.hasPrevious())
                .nextPageUrl(nextPageUrl)
                .previousPageUrl(previousPageUrl)
                .build();
    }

    /* ============================================================
       DELETE / STATUS METHODS
    ============================================================ */

    @Override
    public UserStandardResponse deleteCustomer(String userId) {
        var user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(format("Cannot delete:: No user found with ID:: %s", userId)));

        repository.delete(user);
        logger.info("Deleted user with ID: {}", userId);

        return UserStandardResponse.builder()
                .status(200)
                .message("User deleted successfully")
                .users(List.of(mapper.fromUser(user)))
                .build();
    }

    @Override
    public UserStandardResponse updateUserStatus(String userId, boolean enable) {
        var user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(format("Cannot find user:: %s", userId)));

        user.setStatus(enable ? StatusEnum.ENABLE : StatusEnum.DISABLE);
        var saved = repository.save(user);
        logger.info("Set status for user {} to {}", userId, saved.getStatus());

        return UserStandardResponse.builder()
                .status(200)
                .message("User status updated successfully")
                .users(List.of(mapper.fromUser(saved)))
                .build();
    }

    /* ============================================================
       UTILITY METHODS
    ============================================================ */

    @Override
    public UserStandardResponse existsByID(String userId) {
        boolean exists = repository.existsById(userId);
        logger.info("Checked existence for user ID {}: {}", userId, exists);

        return UserStandardResponse.builder()
                .status(exists ? 200 : 404)
                .message(exists ? "User exists" : "User not found")
                .users(List.of())
                .build();
    }

}
