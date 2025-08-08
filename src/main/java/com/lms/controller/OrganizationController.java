package com.lms.controller;

import com.lms.dto.organization.OrganizationRequest;
import com.lms.dto.organization.OrganizationResponse;
import com.lms.service.OrganizationService;
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
@RequestMapping("/api/v1/organizations")
@Tag(name = "Organization Management", description = "Organization management APIs - SuperAdmin only")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    @Operation(summary = "Create new organization", description = "Create a new organization - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Organization created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "404", description = "Brand not found"),
            @ApiResponse(responseCode = "409", description = "Organization already exists"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<OrganizationResponse> createOrganization(@Valid @RequestBody OrganizationRequest request) {
        OrganizationResponse response = organizationService.createOrganization(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all organizations", description = "Retrieve all organizations - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organizations retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<List<OrganizationResponse>> getAllOrganizations() {
        List<OrganizationResponse> organizations = organizationService.getAllOrganizations();
        return ResponseEntity.ok(organizations);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organization by ID", description = "Retrieve an organization by its ID - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organization retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Organization not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<OrganizationResponse> getOrganizationById(@PathVariable UUID id) {
        OrganizationResponse organization = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(organization);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update organization", description = "Update an existing organization - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organization updated successfully"),
            @ApiResponse(responseCode = "404", description = "Organization not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<OrganizationResponse> updateOrganization(@PathVariable UUID id,
                                                                   @Valid @RequestBody OrganizationRequest request) {
        OrganizationResponse response = organizationService.updateOrganization(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete organization", description = "Delete an organization - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Organization deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Organization not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete organization with existing classrooms or users"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<Void> deleteOrganization(@PathVariable UUID id) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-brand/{brandId}")
    @Operation(summary = "Get organizations by brand", description = "Retrieve organizations by brand ID - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organizations retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Brand not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<List<OrganizationResponse>> getOrganizationsByBrand(@PathVariable UUID brandId) {
        List<OrganizationResponse> organizations = organizationService.getOrganizationsByBrand(brandId);
        return ResponseEntity.ok(organizations);
    }
}