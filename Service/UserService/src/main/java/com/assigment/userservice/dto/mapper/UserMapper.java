package com.assigment.userservice.dto.mapper;

import com.assigment.userservice.dto.request.UserRequest;
import com.assigment.userservice.dto.response.UserResponse;
import com.assigment.userservice.entRepo.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserEntity toUser(UserRequest request) {
        if (request == null) {
            return null;
        }

        UserEntity.UserEntityBuilder builder = UserEntity.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail());

        if (request.getRole() != null) {
            builder.role(request.getRole());
        }

        if (request.getStatus() != null) {
            builder.status(request.getStatus());
        }

        // password handled by service (encoded). Mapper can set raw password if needed,
        // but we will not set encoded here.
        if (request.getPassword() != null) {
            builder.password(request.getPassword());
        }

        return builder.build();
    }

    public UserResponse fromUser(UserEntity userEntity) {
        if (userEntity == null) return null;

        return UserResponse.builder()
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .phoneNumber(userEntity.getPhoneNumber())
                .email(userEntity.getEmail())
                .role(userEntity.getRole())
                .status(userEntity.getStatus())
                .build();
    }
}
