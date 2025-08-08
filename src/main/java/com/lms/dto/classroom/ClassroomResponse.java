package com.lms.dto.classroom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomResponse {

    private UUID id;
    private String name;
    private UUID organizationId;
    private String organizationName;
    private String brandName;
    private int studentCount;
    private int courseCount;
    private int teacherCount;
}