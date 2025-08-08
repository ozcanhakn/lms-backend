package com.lms.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherCourseResponse {

    private UUID id;
    private String name;
    private String imageUrl;
    private UUID classroomId;
    private String classroomName;
}
