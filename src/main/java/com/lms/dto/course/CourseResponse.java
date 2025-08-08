package com.lms.dto.course;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {

    private UUID id;
    private String name;
    private String imageUrl;
    private int assignedClassroomCount;
}
