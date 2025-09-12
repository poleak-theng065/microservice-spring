package com.assigment.userservice.dto.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResetMessage {
    private String email;
    private String token;
}
