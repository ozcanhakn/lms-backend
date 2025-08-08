package com.lms.controller;

import com.lms.dto.brand.BrandRequest;
import com.lms.dto.brand.BrandResponse;
import com.lms.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/brands")
@Tag(name = "Brand Management", description = "Brand management APIs - SuperAdmin only")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @PostMapping
    @Operation(summary = "Create new brand", description = "Create a new brand - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Brand created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "409", description = "Brand already exists"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<BrandResponse> createBrand(@Valid @RequestBody BrandRequest request) {
        BrandResponse response = brandService.createBrand(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all brands", description = "Retrieve all brands - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Brands retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<List<BrandResponse>> getAllBrands() {
        List<BrandResponse> brands = brandService.getAllBrands();
        return ResponseEntity.ok(brands);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get brand by ID", description = "Retrieve a brand by its ID - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Brand retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Brand not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<BrandResponse> getBrandById(@PathVariable UUID id) {
        BrandResponse brand = brandService.getBrandById(id);
        return ResponseEntity.ok(brand);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update brand", description = "Update an existing brand - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Brand updated successfully"),
            @ApiResponse(responseCode = "404", description = "Brand not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<BrandResponse> updateBrand(@PathVariable UUID id,
                                                     @Valid @RequestBody BrandRequest request) {
        BrandResponse response = brandService.updateBrand(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete brand", description = "Delete a brand - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Brand deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Brand not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete brand with existing organizations"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<Void> deleteBrand(@PathVariable UUID id) {
        brandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }
}