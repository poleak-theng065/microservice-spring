package com.assigment.courseservice.controllers;

import com.assigment.courseservice.dto.request.CreateCourseRequest;
import com.assigment.courseservice.dto.request.UpdateCourseRequest;
import com.assigment.courseservice.dto.response.*;
import com.assigment.courseservice.services.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {

    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);
    private final CourseService courseService;

    // =====================================================
    // 🔹 Create course
    // =====================================================
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CourseStandardResponse> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        logger.info("Creating course: {}", request.getCourseName());
        return ResponseEntity.ok(courseService.createCourse(request));
    }

    // =====================================================
    // 🔹 Update course
    // =====================================================
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CourseStandardResponse> updateCourse(
            @PathVariable("id") String courseID,
            @Valid @RequestBody UpdateCourseRequest request) {
        logger.info("Updating course ID: {}", courseID);
        return ResponseEntity.ok(courseService.updateCourse(courseID, request));
    }

    // =====================================================
    // 🔹 Get course by ID
    // =====================================================
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/{id}")
    public ResponseEntity<CourseStandardResponse> getCourseById(@PathVariable("id") String courseID) {
        logger.info("Fetching course ID: {}", courseID);
        return ResponseEntity.ok(courseService.getCourseById(courseID));
    }

    // =====================================================
    // 🔹 Get enabled courses (for everyone)
    // =====================================================
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping
    public ResponseEntity<CoursePaginationResponse> getEnabledCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        logger.debug("Fetching ENABLED courses - page: {}, size: {}", page, size);
        Page<CourseResponse> pagedCourses = courseService.getEnabledCourses(page, size);

        CoursePaginationResponse response = CoursePaginationResponse.builder()
                .status(200)
                .message("Enabled courses fetched successfully")
                .courses(pagedCourses.getContent())
                .currentPage(pagedCourses.getNumber())
                .totalPages(pagedCourses.getTotalPages())
                .totalElements(pagedCourses.getTotalElements())
                .hasNext(pagedCourses.hasNext())
                .hasPrevious(pagedCourses.hasPrevious())
                .nextPageUrl(pagedCourses.hasNext() ? "/course?page=" + (pagedCourses.getNumber() + 1) + "&size=" + size : null)
                .previousPageUrl(pagedCourses.hasPrevious() ? "/course?page=" + (pagedCourses.getNumber() - 1) + "&size=" + size : null)
                .details(null)
                .build();

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // 🔹 Get ALL courses (admin only)
    // =====================================================
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<CoursePaginationResponse> getAllCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        logger.debug("Fetching ALL courses - page: {}, size: {}", page, size);
        Page<CourseResponse> pagedCourses = courseService.getAllCourses(page, size);

        CoursePaginationResponse response = CoursePaginationResponse.builder()
                .status(200)
                .message("All courses fetched successfully")
                .courses(pagedCourses.getContent())
                .currentPage(pagedCourses.getNumber())
                .totalPages(pagedCourses.getTotalPages())
                .totalElements(pagedCourses.getTotalElements())
                .hasNext(pagedCourses.hasNext())
                .hasPrevious(pagedCourses.hasPrevious())
                .nextPageUrl(pagedCourses.hasNext() ? "/course/all?page=" + (pagedCourses.getNumber() + 1) + "&size=" + size : null)
                .previousPageUrl(pagedCourses.hasPrevious() ? "/course/all?page=" + (pagedCourses.getNumber() - 1) + "&size=" + size : null)
                .details(null)
                .build();

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // 🔹 Enable/Disable course (PATCH)
    // =====================================================
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<CourseStandardResponse> updateCourseStatus(
            @PathVariable("id") String courseId,  // keep as String
            @RequestParam("enabled") boolean enabled) {
        logger.info("Updating status for course ID: {} to {}", courseId, enabled ? "ENABLE" : "DISABLE");
        return ResponseEntity.ok(courseService.updateCourseStatus(courseId, enabled));
    }


    // =====================================================
    // 🔹 Delete course
    // =====================================================
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<CourseStandardResponse> deleteCourse(@PathVariable("id") String courseID) {
        logger.warn("Deleting course ID: {}", courseID);
        return ResponseEntity.ok(courseService.deleteCourse(courseID));
    }
}
