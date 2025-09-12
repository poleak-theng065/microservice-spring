package com.assigment.courseservice.dto.mapper;

import com.assigment.courseservice.entRepo.CourseEntity;
import com.assigment.courseservice.dto.request.CreateCourseRequest;
import com.assigment.courseservice.dto.request.UpdateCourseRequest;
import com.assigment.courseservice.dto.response.CourseResponse;

public class CourseMapper {

    public static CourseEntity toEntity(CreateCourseRequest request) {
        return CourseEntity.builder()
                .courseName(request.getCourseName())
                .courseDescription(request.getCourseDescription())
                .courseStatus(request.getCourseStatus())
                .build();
    }

    public static void updateEntity(CourseEntity entity, UpdateCourseRequest request) {
        entity.setCourseName(request.getCourseName());
        entity.setCourseDescription(request.getCourseDescription());
        entity.setCourseStatus(request.getCourseStatus());
    }

    public static CourseResponse toResponse(CourseEntity entity) {
        return CourseResponse.builder()
                .courseID(entity.getCourseID())
                .courseName(entity.getCourseName())
                .courseDescription(entity.getCourseDescription())
                .courseStatus(entity.getCourseStatus())
                .message("Course retrieved successfully!")
                .build();
    }

    public static CourseResponse CourseDisabledResponse(CourseEntity entity) {
        return CourseResponse.builder()
                .courseID(entity.getCourseID())
                .courseName("Course disabled")
                .courseDescription("Course disabled")
                .courseStatus(entity.getCourseStatus())
                .message("Course " + entity.getCourseName() + " is disable!")
                .build();
    }
}
