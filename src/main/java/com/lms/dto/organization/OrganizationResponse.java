package com.lms.dto.organization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {

    private UUID id;
    private String name;
    private UUID brandId;
    private String brandName;
    private String brandCode;
    private int classroomCount;
    private int userCount;
}
