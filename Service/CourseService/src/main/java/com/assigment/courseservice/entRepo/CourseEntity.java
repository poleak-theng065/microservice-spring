package com.assigment.courseservice.entRepo;

import com.assigment.courseservice.constants.Status;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Accessors(chain = true)
@Validated
@Entity
@Table(name = "courses_table")
public class CourseEntity implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    private String courseID;

    @Column(nullable = false)
    private String courseName;

    @Column(nullable = false, length = 1000)
    private String courseDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status courseStatus;

    @CreationTimestamp
    @Column(nullable = false, name = "created_at", updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(nullable = false, name = "updated_at")
    private Date updatedAt;

}
