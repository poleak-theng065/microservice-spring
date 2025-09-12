package com.assigment.courseservice.controllers;

import com.assigment.courseservice.dto.request.CreateCourseRequest;
import com.assigment.courseservice.dto.request.UpdateCourseRequest;
import com.assigment.courseservice.dto.response.CourseResponse;
import com.assigment.courseservice.services.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {

    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);
    private final CourseService courseService;

    // =====================================================
    // 🔹 ADMIN ENDPOINTS
    // =====================================================
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        logger.info("Creating new course: {}", request.getCourseName());
        try {
            CourseResponse response = courseService.createCourse(request);
            logger.info("Course created successfully: {}", response.getCourseID());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to create course: {}", request.getCourseName(), e);
            throw e;
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable("id") String courseID,
            @Valid @RequestBody UpdateCourseRequest request) {
        logger.info("Updating course ID: {}", courseID);
        try {
            CourseResponse response = courseService.updateCourse(courseID, request);
            logger.info("Course updated successfully: {}", courseID);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to update course: {}", courseID, e);
            throw e;
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<CourseResponse> changeCourseStatus(
            @PathVariable("id") String courseID,
            @RequestParam("enabled") boolean enabled) {
        logger.info("Changing status for course ID: {} to enabled: {}", courseID, enabled);
        try {
            CourseResponse response = enabled
                    ? courseService.enableCourse(courseID)
                    : courseService.disableCourse(courseID);
            logger.info("Course status changed successfully: {}", courseID);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to change status for course: {}", courseID, e);
            throw e;
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<CourseResponse>> getAllCoursesForAdmin() {
        logger.debug("Fetching all courses for admin");
        try {
            List<CourseResponse> courses = courseService.getAllCoursesForAdmin();
            logger.info("Retrieved {} courses for admin", courses.size());
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            logger.error("Failed to fetch courses for admin", e);
            throw e;
        }
    }

    // =====================================================
    // 🔹 SHARED ENDPOINTS (Admin & User)
    // =====================================================
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable("id") String courseID) {
        logger.debug("Fetching course by ID: {}", courseID);
        try {
            CourseResponse course = courseService.getCourseById(courseID);
            logger.info("Course retrieved: {}", courseID);
            return ResponseEntity.ok(course);
        } catch (Exception e) {
            logger.error("Failed to fetch course: {}", courseID, e);
            throw e;
        }
    }

    // =====================================================
    // 🔹 USER ENDPOINTS
    // =====================================================
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping
    public ResponseEntity<List<CourseResponse>> getEnabledCourses() {
        logger.debug("Fetching enabled courses for user");
        try {
            List<CourseResponse> courses = courseService.getAllCoursesForUser();
            logger.info("Retrieved {} enabled courses for user", courses.size());
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            logger.error("Failed to fetch enabled courses", e);
            throw e;
        }
    }
}