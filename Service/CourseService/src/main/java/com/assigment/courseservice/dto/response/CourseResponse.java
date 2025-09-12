package com.assigment.courseservice.dto.response;

import com.assigment.courseservice.constants.Status;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {
    private String courseID;
    private String courseName;
    private String courseDescription;
    private Status courseStatus;
    private String message;
}
