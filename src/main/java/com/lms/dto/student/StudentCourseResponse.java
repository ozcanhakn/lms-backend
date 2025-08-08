package com.lms.dto.student;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentCourseResponse {

    private UUID id;
    private String name;
    private String imageUrl;
    private String classroomName;
}