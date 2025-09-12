package com.assigment.courseservice.services.impl;

import com.assigment.courseservice.constants.Status;
import com.assigment.courseservice.dto.mapper.CourseMapper;
import com.assigment.courseservice.dto.request.CreateCourseRequest;
import com.assigment.courseservice.dto.request.UpdateCourseRequest;
import com.assigment.courseservice.dto.response.CourseErrorResponse;
import com.assigment.courseservice.dto.response.CourseResponse;
import com.assigment.courseservice.entRepo.CourseEntity;
import com.assigment.courseservice.entRepo.CourseRepository;
import com.assigment.courseservice.exceptions.ResourceNotFoundException;
import com.assigment.courseservice.services.CourseService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseServiceImpl implements CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseServiceImpl.class);
    private final CourseRepository courseRepository;

    @Override
    public CourseResponse createCourse(CreateCourseRequest request) {
        logger.info("Creating new course: {}", request.getCourseName());
        logger.debug("Course request details: {}", request);

        try {
            if (request.getCourseStatus() == null) {
                logger.debug("Setting default status to ENABLE for course: {}", request.getCourseName());
                request.setCourseStatus(Status.ENABLE);
            }

            CourseEntity entity = CourseMapper.toEntity(request);
            logger.debug("Mapped to entity: {}", entity);

            CourseEntity saved = courseRepository.save(entity);
            logger.info("Course created successfully with ID: {}", saved.getCourseID());
            logger.debug("Saved course details: {}", saved);

            return CourseMapper.toResponse(saved);

        } catch (Exception e) {
            logger.error("Failed to create course: {}", request.getCourseName(), e);
            throw e;
        }
    }

    @Override
    public CourseResponse updateCourse(String courseID, UpdateCourseRequest request) {
        logger.info("Updating course ID: {}", courseID);
        logger.debug("Update request details: {}", request);

        try {
            CourseEntity entity = courseRepository.findById(courseID)
                    .orElseThrow(() -> {
                        logger.warn("Course not found for update: {}", courseID);
                        return new EntityNotFoundException("Course not found");
                    });

            logger.debug("Found course to update: {}", entity.getCourseName());
            CourseMapper.updateEntity(entity, request);
            logger.debug("Course entity updated with new details");

            CourseEntity updated = courseRepository.save(entity);
            logger.info("Course updated successfully: {}", courseID);

            return CourseMapper.toResponse(updated);

        } catch (Exception e) {
            logger.error("Failed to update course: {}", courseID, e);
            throw e;
        }
    }

    @Override
    public CourseResponse getCourseById(String courseID) {
        logger.debug("Fetching course by ID: {}", courseID);

        try {
            CourseEntity course = courseRepository.findById(courseID)
                    .orElseThrow(() -> {
                        logger.warn("Course not found: {}", courseID);
                        return new ResourceNotFoundException("Course not found with ID: " + courseID);
                    });

            // Get current user role
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String role = authentication.getAuthorities().iterator().next().getAuthority();
            logger.debug("User role: {}, Course status: {}", role, course.getCourseStatus());

            // If course is DISABLE and user is not ADMIN → block
            if (course.getCourseStatus() == Status.DISABLE && !role.equals("ROLE_ADMIN")) {
                logger.warn("Access denied - User {} attempted to access disabled course: {}",
                        authentication.getName(), courseID);
                return CourseMapper.CourseDisabledResponse(course);
            }

            logger.info("Course retrieved successfully: {} - {}", courseID, course.getCourseName());
            return CourseMapper.toResponse(course);

        } catch (Exception e) {
            logger.error("Failed to fetch course: {}", courseID, e);
            throw e;
        }
    }

    @Override
    public List<CourseResponse> getAllCoursesForAdmin() {
        logger.debug("Fetching all courses for admin");

        try {
            List<CourseEntity> courses = courseRepository.findAll();
            logger.info("Retrieved {} total courses for admin", courses.size());
            logger.debug("Courses: {}", courses.stream()
                    .map(c -> c.getCourseID() + ":" + c.getCourseName())
                    .collect(Collectors.toList()));

            return courses.stream()
                    .map(CourseMapper::toResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Failed to fetch courses for admin", e);
            throw e;
        }
    }

    @Override
    public List<CourseResponse> getAllCoursesForUser() {
        logger.debug("Fetching enabled courses for user");

        try {
            List<CourseEntity> allCourses = courseRepository.findAll();
            List<CourseEntity> enabledCourses = allCourses.stream()
                    .filter(c -> c.getCourseStatus() == Status.ENABLE)
                    .collect(Collectors.toList());

            logger.info("Retrieved {} enabled courses out of {} total for user",
                    enabledCourses.size(), allCourses.size());
            logger.debug("Enabled courses: {}", enabledCourses.stream()
                    .map(c -> c.getCourseID() + ":" + c.getCourseName())
                    .collect(Collectors.toList()));

            return enabledCourses.stream()
                    .map(CourseMapper::toResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Failed to fetch enabled courses for user", e);
            throw e;
        }
    }

    @Override
    public CourseResponse enableCourse(String courseID) {
        logger.info("Enabling course: {}", courseID);

        try {
            CourseEntity entity = courseRepository.findById(courseID)
                    .orElseThrow(() -> {
                        logger.warn("Course not found for enabling: {}", courseID);
                        return new EntityNotFoundException("Course not found");
                    });

            logger.debug("Current course status: {}", entity.getCourseStatus());
            entity.setCourseStatus(Status.ENABLE);

            CourseEntity updated = courseRepository.save(entity);
            logger.info("Course enabled successfully: {}", courseID);

            return CourseMapper.toResponse(updated);

        } catch (Exception e) {
            logger.error("Failed to enable course: {}", courseID, e);
            throw e;
        }
    }

    @Override
    public CourseResponse disableCourse(String courseID) {
        logger.info("Disabling course: {}", courseID);

        try {
            CourseEntity entity = courseRepository.findById(courseID)
                    .orElseThrow(() -> {
                        logger.warn("Course not found for disabling: {}", courseID);
                        return new EntityNotFoundException("Course not found");
                    });

            logger.debug("Current course status: {}", entity.getCourseStatus());
            entity.setCourseStatus(Status.DISABLE);

            CourseEntity updated = courseRepository.save(entity);
            logger.info("Course disabled successfully: {}", courseID);

            return CourseMapper.toResponse(updated);

        } catch (Exception e) {
            logger.error("Failed to disable course: {}", courseID, e);
            throw e;
        }
    }
}