package com.lms.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherClassroomResponse {

    private UUID id;
    private String name;
    private UUID organizationId;
    private String organizationName;
    private int studentCount;
    private int courseCount;
}
