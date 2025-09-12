package com.assigment.userservice.dto.response;

import java.util.Map;

public record ErrorResponse(
        Map<String, String> errors
) {

}
