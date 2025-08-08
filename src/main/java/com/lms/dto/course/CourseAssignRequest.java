package com.lms.dto.course;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseAssignRequest {

    @NotNull(message = "Course ID is required")
    private UUID courseId;

    @NotNull(message = "Classroom ID is required")
    private UUID classroomId;
}
