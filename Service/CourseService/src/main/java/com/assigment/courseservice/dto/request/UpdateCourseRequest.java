package com.assigment.courseservice.dto.request;

import com.assigment.courseservice.constants.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCourseRequest {

    @NotBlank(message = "Course name is required")
    private String courseName;

    @NotBlank(message = "Course description is required")
    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String courseDescription;

    private Status courseStatus; // can change ENABLED / DISABLED
}
