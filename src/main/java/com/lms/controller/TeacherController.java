package com.lms.controller;

import com.lms.dto.teacher.*;
import com.lms.entity.User;
import com.lms.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teachers")
@Tag(name = "Teacher Management", description = "Teacher management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    // SuperAdmin endpoint
    @PostMapping("/assign-classroom")
    @Operation(summary = "Assign teacher to classroom", description = "Assign a teacher to a classroom - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Teacher assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "404", description = "Teacher or Classroom not found"),
            @ApiResponse(responseCode = "409", description = "Teacher already assigned to classroom"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> assignTeacherToClassroom(@Valid @RequestBody TeacherAssignClassroomRequest request) {
        teacherService.assignTeacherToClassroom(request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    // SuperAdmin endpoint
    @DeleteMapping("/assign-classroom")
    @Operation(summary = "Unassign teacher from classroom", description = "Unassign a teacher from a classroom - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Teacher unassigned successfully"),
            @ApiResponse(responseCode = "404", description = "Teacher assignment not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> unassignTeacherFromClassroom(@RequestParam UUID teacherId,
                                                             @RequestParam UUID classroomId) {
        teacherService.unassignTeacherFromClassroom(teacherId, classroomId);
        return ResponseEntity.noContent().build();
    }

    // Teacher endpoints - PDF requirement
    @GetMapping("/my-classes")
    @Operation(summary = "Get teacher's classes", description = "Get classrooms assigned to the authenticated teacher")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teacher's classes retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Teacher role required"),
            @ApiResponse(responseCode = "404", description = "Teacher not found")
    })
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<TeacherClassroomResponse>> getMyClasses(Authentication authentication) {
        User teacher = (User) authentication.getPrincipal();
        List<TeacherClassroomResponse> classrooms = teacherService.getTeacherClassrooms(teacher.getId());
        return ResponseEntity.ok(classrooms);
    }

    @GetMapping("/my-students")
    @Operation(summary = "Get teacher's students", description = "Get all students in classrooms assigned to the authenticated teacher")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teacher's students retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Teacher role required"),
            @ApiResponse(responseCode = "404", description = "Teacher not found")
    })
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<TeacherStudentResponse>> getMyStudents(Authentication authentication) {
        User teacher = (User) authentication.getPrincipal();
        List<TeacherStudentResponse> students = teacherService.getTeacherStudents(teacher.getId());
        return ResponseEntity.ok(students);
    }

    @GetMapping("/my-courses")
    @Operation(summary = "Get teacher's courses", description = "Get all courses in classrooms assigned to the authenticated teacher")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teacher's courses retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Teacher role required"),
            @ApiResponse(responseCode = "404", description = "Teacher not found")
    })
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<TeacherCourseResponse>> getMyCourses(Authentication authentication) {
        User teacher = (User) authentication.getPrincipal();
        List<TeacherCourseResponse> courses = teacherService.getTeacherCourses(teacher.getId());
        return ResponseEntity.ok(courses);
    }

    // ogretmen gorevlerini yonetmek icin
    @GetMapping("/{teacherId}/classes")
    @Operation(summary = "Get teacher's classes by ID", description = "Get classrooms assigned to a specific teacher - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teacher's classes retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required"),
            @ApiResponse(responseCode = "404", description = "Teacher not found")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<TeacherClassroomResponse>> getTeacherClasses(@PathVariable UUID teacherId) {
        List<TeacherClassroomResponse> classrooms = teacherService.getTeacherClassrooms(teacherId);
        return ResponseEntity.ok(classrooms);
    }

    @GetMapping("/{teacherId}/students")
    @Operation(summary = "Get teacher's students by ID", description = "Get students for a specific teacher - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teacher's students retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required"),
            @ApiResponse(responseCode = "404", description = "Teacher not found")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<TeacherStudentResponse>> getTeacherStudents(@PathVariable UUID teacherId) {
        List<TeacherStudentResponse> students = teacherService.getTeacherStudents(teacherId);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{teacherId}/courses")
    @Operation(summary = "Get teacher's courses by ID", description = "Get courses for a specific teacher - SuperAdmin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teacher's courses retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - SuperAdmin role required"),
            @ApiResponse(responseCode = "404", description = "Teacher not found")
    })
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<TeacherCourseResponse>> getTeacherCourses(@PathVariable UUID teacherId) {
        List<TeacherCourseResponse> courses = teacherService.getTeacherCourses(teacherId);
        return ResponseEntity.ok(courses);
    }
}