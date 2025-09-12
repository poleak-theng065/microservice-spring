package com.assigment.courseservice.services;

import com.assigment.courseservice.dto.request.CreateCourseRequest;
import com.assigment.courseservice.dto.request.UpdateCourseRequest;
import com.assigment.courseservice.dto.response.CourseResponse;

import java.util.List;

public interface CourseService {

    CourseResponse createCourse(CreateCourseRequest request);

    CourseResponse updateCourse(String courseID, UpdateCourseRequest request);

    CourseResponse getCourseById(String courseID);

    List<CourseResponse> getAllCoursesForAdmin();   // admin → see all

    List<CourseResponse> getAllCoursesForUser();    // user → only enabled

    CourseResponse enableCourse(String courseID);

    CourseResponse disableCourse(String courseID);
}
