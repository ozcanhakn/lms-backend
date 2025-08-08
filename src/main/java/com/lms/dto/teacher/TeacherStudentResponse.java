package com.lms.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherStudentResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private UUID classroomId;
    private String classroomName;
}