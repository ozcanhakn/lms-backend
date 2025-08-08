package com.lms.controller;

import com.lms.dto.course.CourseAssignRequest;
import com.lms.dto.course.CourseRequest;
import com.lms.dto.course.CourseResponse;
import com.lms.service.CourseService;
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
@RequestMapping("/api/v1/courses")
@Tag(name = "Course Management", description = "Course management APIs - SuperAdmin only")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    @Operation(summary = "Create new course", description = "Create a new course - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Course created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "409", description = "Course already exists"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request) {
        CourseResponse response = courseService.createCourse(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/assign")
    @Operation(summary = "Assign course to classroom", description = "Assign a course to a classroom - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Course assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "404", description = "Course or Classroom not found"),
            @ApiResponse(responseCode = "409", description = "Course already assigned to classroom"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<Void> assignCourseToClassroom(@Valid @RequestBody CourseAssignRequest request) {
        courseService.assignCourseToClassroom(request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("/assign")
    @Operation(summary = "Unassign course from classroom", description = "Unassign a course from a classroom - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Course unassigned successfully"),
            @ApiResponse(responseCode = "404", description = "Course assignment not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<Void> unassignCourseFromClassroom(@RequestParam UUID courseId,
                                                            @RequestParam UUID classroomId) {
        courseService.unassignCourseFromClassroom(courseId, classroomId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get all courses", description = "Retrieve all courses - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Courses retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        List<CourseResponse> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get course by ID", description = "Retrieve a course by its ID - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Course not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable UUID id) {
        CourseResponse course = courseService.getCourseById(id);
        return ResponseEntity.ok(course);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update course", description = "Update an existing course - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course updated successfully"),
            @ApiResponse(responseCode = "404", description = "Course not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<CourseResponse> updateCourse(@PathVariable UUID id,
                                                       @Valid @RequestBody CourseRequest request) {
        CourseResponse response = courseService.updateCourse(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete course", description = "Delete a course - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Course deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Course not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete course with existing assignments"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<Void> deleteCourse(@PathVariable UUID id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-classroom/{classroomId}")
    @Operation(summary = "Get courses by classroom", description = "Retrieve courses assigned to a classroom - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Courses retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Classroom not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<List<CourseResponse>> getCoursesByClassroom(@PathVariable UUID classroomId) {
        List<CourseResponse> courses = courseService.getCoursesByClassroom(classroomId);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/available/{classroomId}")
    @Operation(summary = "Get available courses for classroom", description = "Get courses not assigned to a specific classroom - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Available courses retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Classroom not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    public ResponseEntity<List<CourseResponse>> getAvailableCoursesForClassroom(@PathVariable UUID classroomId) {
        List<CourseResponse> courses = courseService.getAvailableCoursesForClassroom(classroomId);
        return ResponseEntity.ok(courses);
    }
}