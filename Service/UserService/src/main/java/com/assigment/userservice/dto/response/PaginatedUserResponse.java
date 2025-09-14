package com.assigment.userservice.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedUserResponse {
    private int status;
    private String message;
    private List<UserResponse> users;

    // pagination metadata
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean hasNext;
    private boolean hasPrevious;

    // pagination links
    private String nextPageUrl;
    private String previousPageUrl;
}
