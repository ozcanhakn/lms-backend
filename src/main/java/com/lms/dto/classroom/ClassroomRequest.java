package com.lms.dto.classroom;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomRequest {

    @NotBlank(message = "Classroom name is required")
    @Size(min = 2, max = 100, message = "Classroom name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Organization ID is required")
    private UUID organizationId;
}