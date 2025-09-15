package com.assigment.courseservice.entRepo;

import com.assigment.courseservice.constants.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<CourseEntity, String> {
    Page<CourseEntity> findByCourseStatus(Status courseStatus, Pageable pageable);
}
