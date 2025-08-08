package com.lms.controller;

import com.lms.dto.user.CreateUserRequest;
import com.lms.dto.user.UserResponse;
import com.lms.service.UserService;
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
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "User management APIs - SuperAdmin only")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Create new user", description = "Create a new user (Teacher or Student) - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "404", description = "Organization or Classroom not found"),
            @ApiResponse(responseCode = "409", description = "User already exists"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve all users - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve a user by its ID - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update an existing user - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id,
                                                   @Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Delete a user - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete SuperAdmin users"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/teachers")
    @Operation(summary = "Get all teachers", description = "Retrieve all teachers - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teachers retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<List<UserResponse>> getTeachers() {
        List<UserResponse> teachers = userService.getTeachers();
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/students")
    @Operation(summary = "Get all students", description = "Retrieve all students - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Students retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<List<UserResponse>> getStudents() {
        List<UserResponse> students = userService.getStudents();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/by-organization/{organizationId}")
    @Operation(summary = "Get users by organization", description = "Retrieve users by organization ID - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Organization not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<List<UserResponse>> getUsersByOrganization(@PathVariable UUID organizationId) {
        List<UserResponse> users = userService.getUsersByOrganization(organizationId);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/by-classroom/{classroomId}")
    @Operation(summary = "Get students by classroom", description = "Retrieve students by classroom ID - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Students retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Classroom not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<List<UserResponse>> getStudentsByClassroom(@PathVariable UUID classroomId) {
        List<UserResponse> students = userService.getStudentsByClassroom(classroomId);
        return ResponseEntity.ok(students);
    }
}