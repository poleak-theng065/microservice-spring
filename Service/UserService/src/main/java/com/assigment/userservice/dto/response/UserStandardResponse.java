package com.assigment.userservice.dto.response;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStandardResponse {
    private int status;
    private String message;
    private List<UserResponse> users;
}
