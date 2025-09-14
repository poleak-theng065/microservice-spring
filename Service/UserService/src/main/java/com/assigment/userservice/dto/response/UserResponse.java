package com.assigment.userservice.dto.response;

import com.assigment.userservice.constants.RoleEnum;
import com.assigment.userservice.constants.StatusEnum;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private RoleEnum role;
    private StatusEnum status;
}
