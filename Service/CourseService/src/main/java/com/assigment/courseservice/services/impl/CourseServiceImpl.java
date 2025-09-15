package com.assigment.courseservice.services.impl;

import com.assigment.courseservice.constants.Status;
import com.assigment.courseservice.dto.mapper.CourseMapper;
import com.assigment.courseservice.dto.request.CreateCourseRequest;
import com.assigment.courseservice.dto.request.UpdateCourseRequest;
import com.assigment.courseservice.dto.response.CourseResponse;
import com.assigment.courseservice.dto.response.CourseStandardResponse;
import com.assigment.courseservice.entRepo.CourseEntity;
import com.assigment.courseservice.entRepo.CourseRepository;
import com.assigment.courseservice.exceptions.ResourceNotFoundException;
import com.assigment.courseservice.services.CourseService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseServiceImpl.class);

    private final CourseRepository repository;

    // Create course
    @Override
    @Transactional
    public CourseStandardResponse createCourse(CreateCourseRequest request) {
        CourseEntity entity = CourseMapper.toEntity(request);
        entity.setCourseStatus(Status.ENABLE); // default enabled
        repository.save(entity);

        return CourseStandardResponse.builder()
                .status(200)
                .message("Course created successfully")
                .course(CourseMapper.toResponse(entity))
                .build();
    }

    // Update course
    @Override
    @Transactional
    public CourseStandardResponse updateCourse(String courseId, UpdateCourseRequest request) {
        CourseEntity course = repository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id " + courseId));

        CourseMapper.updateEntity(course, request);
        repository.save(course);

        return CourseStandardResponse.builder()
                .status(200)
                .message("Course updated successfully")
                .course(CourseMapper.toResponse(course))
                .build();
    }

    // Get by ID
    @Override
    public CourseStandardResponse getCourseById(String courseId) {
        CourseEntity course = repository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id " + courseId));

        return CourseStandardResponse.builder()
                .status(200)
                .message("Course fetched successfully")
                .course(CourseMapper.toResponse(course))
                .build();
    }

    // Get enabled courses
    @Override
    public Page<CourseResponse> getEnabledCourses(int page, int size) {
        return repository.findByCourseStatus(Status.ENABLE, PageRequest.of(page, size))
                .map(CourseMapper::toResponse);
    }

    // Get all courses
    @Override
    public Page<CourseResponse> getAllCourses(int page, int size) {
        return repository.findAll(PageRequest.of(page, size))
                .map(CourseMapper::toResponse);
    }

    // Update course status (PATCH)
    @Override
    @Transactional
    public CourseStandardResponse updateCourseStatus(String courseId, boolean enabled) {
        CourseEntity course = repository.findById(courseId) // repository ID is String
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id " + courseId));

        course.setCourseStatus(enabled ? Status.ENABLE : Status.DISABLE);
        repository.save(course);

        return CourseStandardResponse.builder()
                .status(200)
                .message("Course status updated successfully")
                .course(CourseMapper.toResponse(course))
                .build();
    }

    // Delete course
    @Override
    @Transactional
    public CourseStandardResponse deleteCourse(String courseId) {
        CourseEntity course = repository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id " + courseId));

        repository.delete(course);

        return CourseStandardResponse.builder()
                .status(200)
                .message("Course deleted successfully")
                .course(CourseMapper.toResponse(course))
                .build();
    }
}
