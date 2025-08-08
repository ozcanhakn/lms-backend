package com.lms.dto.brand;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrandResponse {

    private UUID id;
    private String name;
    private String code;
    private int organizationCount;
}
