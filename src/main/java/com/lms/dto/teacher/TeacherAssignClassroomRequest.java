package com.lms.dto.teacher;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAssignClassroomRequest {

    @NotNull(message = "Teacher ID is required")
    private UUID teacherId;

    @NotNull(message = "Classroom ID is required")
    private UUID classroomId;
}
