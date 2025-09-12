package com.assigment.courseservice.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseErrorResponse {
    private String courseID;
    private String message;
}
