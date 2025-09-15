package com.assigment.courseservice.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoursePaginationResponse {
    private int status;
    private String message;
    private List<CourseResponse> courses;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean hasNext;
    private boolean hasPrevious;
    private String nextPageUrl;
    private String previousPageUrl;
    private Object details;
}
