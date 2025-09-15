package com.assigment.courseservice.services;

import com.assigment.courseservice.dto.request.CreateCourseRequest;
import com.assigment.courseservice.dto.request.UpdateCourseRequest;
import com.assigment.courseservice.dto.response.CoursePaginationResponse;
import com.assigment.courseservice.dto.response.CourseStandardResponse;
import com.assigment.courseservice.dto.response.CourseResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface CourseService {

    CourseStandardResponse createCourse(CreateCourseRequest request);

    CourseStandardResponse updateCourse(String courseId, UpdateCourseRequest request);

    CourseStandardResponse getCourseById(String courseId);

    Page<CourseResponse> getEnabledCourses(int page, int size);

    Page<CourseResponse> getAllCourses(int page, int size);

    CourseStandardResponse updateCourseStatus(String courseId, boolean enabled);

    CourseStandardResponse deleteCourse(String courseId);
}
