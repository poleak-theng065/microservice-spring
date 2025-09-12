package com.assigment.userservice.services;

import com.assigment.userservice.constants.RoleEnum;
import com.assigment.userservice.constants.Status;
import com.assigment.userservice.dto.mapper.UserMapper;
import com.assigment.userservice.dto.request.UserRequest;
import com.assigment.userservice.dto.response.UserResponse;
import com.assigment.userservice.entRepo.UserEntity;
import com.assigment.userservice.entRepo.UserRepository;
import com.assigment.userservice.exceptions.UserNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LogManager.getLogger(UserService.class);

    private final UserRepository repository;
    private final UserMapper mapper;

    public UserResponse createUser(UserRequest request) {
        var user = this.repository.save(mapper.toUser(request));
        logger.info("Created new user with ID: {}", user.getUserID());
        return mapper.fromUser(user);
    }

    public void updateUser(@Valid UserRequest request) {
        var user = this.repository.findById(String.valueOf(request.userID()))
                .orElseThrow(() -> {
                    logger.error("Failed to update user: No user found with ID {}", request.userID());
                    return new UserNotFoundException(
                            format("Cannot update customer:: No user found with the provided ID:: %s", request.userID())
                    );
                });

        mergerUser(user, request);
        repository.save(user);
        logger.info("Updated user with ID: {}", user.getUserID());
    }

    private void mergerUser(UserEntity user, UserRequest request) {
        if (StringUtils.isNotBlank(request.firstName())) user.setFirstName(request.firstName());
        if (StringUtils.isNotBlank(request.lastName())) user.setLastName(request.lastName());
        if (StringUtils.isNotBlank(request.email())) user.setEmail(request.email());
        if (StringUtils.isNotBlank(request.phoneNumber())) user.setPhoneNumber(request.phoneNumber());
        if (StringUtils.isNotBlank(request.password())) user.setPassword(request.password());
        if (StringUtils.isNotBlank(request.role())) user.setRole(RoleEnum.valueOf(request.role()));
        if (StringUtils.isNotBlank(request.status())) user.setStatus(Status.valueOf(request.status()));
    }

    public List<UserResponse> findAllUsers() {
        List<UserResponse> users = repository.findAll()
                .stream()
                .map(mapper::fromUser)
                .collect(Collectors.toList());
        logger.info("Fetched all users. Total count: {}", users.size());
        return users;
    }

    public Boolean existsByID(String userId) {
        boolean exists = repository.findById(userId).isPresent();
        logger.info("Checked existence for user ID {}: {}", userId, exists);
        return exists;
    }

    public UserResponse FindById(String userId) {
        return repository.findById(userId)
                .map(mapper::fromUser)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", userId);
                    return new UserNotFoundException(format("Cannot find user:: %s", userId));
                });
    }

    public UserResponse FindByEmail(String email) {
        UserEntity user = repository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", email);
                    return new RuntimeException("User not found");
                });
        logger.info("Fetched user by email: {}", email);
        return mapper.fromUser(user);
    }

    public void deleteCustomer(String userId) {
        repository.deleteById(userId);
        logger.info("Deleted user with ID: {}", userId);
    }
}
