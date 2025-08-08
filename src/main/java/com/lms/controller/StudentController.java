package com.lms.controller;

import com.lms.dto.student.StudentCourseResponse;
import com.lms.entity.User;
import com.lms.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/students")
@Tag(name = "Student Management", description = "Student management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // Student endpoint
    @GetMapping("/my-courses")
    @Operation(summary = "Get student's courses", description = "Get courses assigned to the authenticated student's classroom")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Student's courses retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Student role required"),
            @ApiResponse(responseCode = "404", description = "Student not found or not assigned to classroom"),
            @ApiResponse(responseCode = "400", description = "Student is not assigned to any classroom")
    })
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<StudentCourseResponse>> getMyCourses(Authentication authentication) {
        User student = (User) authentication.getPrincipal();
        List<StudentCourseResponse> courses = studentService.getStudentCourses(student.getId());
        return ResponseEntity.ok(courses);
    }

    // Herhangi bir ogrencinin kurslari almasi icin ve superadmin uc noktasi
    @GetMapping("/{studentId}/courses")
    @Operation(summary = "Get student's courses by ID", description = "Get courses for a specific student - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Student's courses retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required"),
            @ApiResponse(responseCode = "404", description = "Student not found"),
            @ApiResponse(responseCode = "400", description = "User is not a student or not assigned to classroom")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<StudentCourseResponse>> getStudentCourses(@PathVariable UUID studentId) {
        List<StudentCourseResponse> courses = studentService.getStudentCourses(studentId);
        return ResponseEntity.ok(courses);
    }

    // Sinif bazinda dersleri almak icin
    @GetMapping("/classroom/{classroomId}/courses")
    @Operation(summary = "Get courses by classroom", description = "Get courses assigned to a specific classroom - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Classroom courses retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required"),
            @ApiResponse(responseCode = "404", description = "Classroom not found")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<StudentCourseResponse>> getCoursesByClassroom(@PathVariable UUID classroomId) {
        List<StudentCourseResponse> courses = studentService.getCoursesByClassroom(classroomId);
        return ResponseEntity.ok(courses);
    }
}