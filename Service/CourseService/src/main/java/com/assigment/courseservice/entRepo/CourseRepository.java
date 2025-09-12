package com.assigment.courseservice.entRepo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<CourseEntity, String> {

    Optional<CourseEntity> findByCourseID(String courseID);

}
