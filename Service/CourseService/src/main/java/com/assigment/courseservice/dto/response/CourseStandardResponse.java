package com.assigment.courseservice.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseStandardResponse {
    private int status;
    private String message;
    private CourseResponse course;
}
