package com.assigment.userservice.dto.response;

import com.assigment.userservice.constants.RoleEnum;
import com.assigment.userservice.constants.Status;
import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private RoleEnum role;
    private Status status;

    // getters and setters (or use Lombok)
}
