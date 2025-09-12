package com.assigment.userservice.dto.mapper;

import com.assigment.userservice.constants.RoleEnum;
import com.assigment.userservice.constants.Status;
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
        return UserEntity.builder()
                .userID(request.userID())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phoneNumber(request.phoneNumber())
                .email(request.email())
                .password(request.password())
                .role(RoleEnum.valueOf(request.role()))
                .status(Status.valueOf(request.status()))
                .build();
    }

    public UserResponse fromUser(UserEntity userEntity) {
        return new UserResponse(
                userEntity.getFirstName(),
                userEntity.getLastName(),
                userEntity.getPhoneNumber(),
                userEntity.getEmail(),
                userEntity.getRole(),
                userEntity.getStatus()
        );
    }
}
