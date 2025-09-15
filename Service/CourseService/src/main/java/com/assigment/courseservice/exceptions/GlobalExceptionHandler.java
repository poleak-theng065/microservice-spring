package com.assigment.courseservice.exceptions;

import com.assigment.courseservice.dto.response.CourseErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CourseErrorResponse> handleGenericException(Exception ex) {
        CourseErrorResponse response = CourseErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Internal server error")
                .details(CourseErrorResponse.ErrorDetails.builder()
                        .error(ex.getClass().getSimpleName())
                        .message(ex.getMessage())
                        .build())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<CourseErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        CourseErrorResponse response = CourseErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .message("Data integrity violation")
                .details(CourseErrorResponse.ErrorDetails.builder()
                        .error("DataIntegrityViolationException")
                        .message(ex.getMostSpecificCause().getMessage())
                        .build())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<CourseErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        CourseErrorResponse response = CourseErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message("Resource not found")
                .details(CourseErrorResponse.ErrorDetails.builder()
                        .error("ResourceNotFoundException")
                        .message(ex.getMessage())
                        .build())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}
