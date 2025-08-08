package com.lms.controller;

import com.lms.dto.classroom.ClassroomRequest;
import com.lms.dto.classroom.ClassroomResponse;
import com.lms.service.ClassroomService;
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
@RequestMapping("/api/v1/classrooms")
@Tag(name = "Classroom Management", description = "Classroom management APIs - SuperAdmin only")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class ClassroomController {

    private final ClassroomService classroomService;

    public ClassroomController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @PostMapping
    @Operation(summary = "Create new classroom", description = "Create a new classroom - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Classroom created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "404", description = "Organization not found"),
            @ApiResponse(responseCode = "409", description = "Classroom already exists"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<ClassroomResponse> createClassroom(@Valid @RequestBody ClassroomRequest request) {
        ClassroomResponse response = classroomService.createClassroom(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all classrooms", description = "Retrieve all classrooms - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Classrooms retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<List<ClassroomResponse>> getAllClassrooms() {
        List<ClassroomResponse> classrooms = classroomService.getAllClassrooms();
        return ResponseEntity.ok(classrooms);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get classroom by ID", description = "Retrieve a classroom by its ID - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Classroom retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Classroom not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<ClassroomResponse> getClassroomById(@PathVariable UUID id) {
        ClassroomResponse classroom = classroomService.getClassroomById(id);
        return ResponseEntity.ok(classroom);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update classroom", description = "Update an existing classroom - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Classroom updated successfully"),
            @ApiResponse(responseCode = "404", description = "Classroom not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<ClassroomResponse> updateClassroom(@PathVariable UUID id,
                                                             @Valid @RequestBody ClassroomRequest request) {
        ClassroomResponse response = classroomService.updateClassroom(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete classroom", description = "Delete a classroom - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Classroom deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Classroom not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete classroom with existing students or courses"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<Void> deleteClassroom(@PathVariable UUID id) {
        classroomService.deleteClassroom(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-organization/{organizationId}")
    @Operation(summary = "Get classrooms by organization", description = "Retrieve classrooms by organization ID - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Classrooms retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Organization not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<List<ClassroomResponse>> getClassroomsByOrganization(@PathVariable UUID organizationId) {
        List<ClassroomResponse> classrooms = classroomService.getClassroomsByOrganization(organizationId);
        return ResponseEntity.ok(classrooms);
    }
}