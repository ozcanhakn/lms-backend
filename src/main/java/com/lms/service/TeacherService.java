package com.lms.service;

import com.lms.dto.teacher.*;
import com.lms.entity.*;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class TeacherService {

    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;
    private final TeacherClassroomAssignmentRepository teacherClassroomAssignmentRepository;
    private final CourseRepository courseRepository;

    public TeacherService(UserRepository userRepository,
                          ClassroomRepository classroomRepository,
                          TeacherClassroomAssignmentRepository teacherClassroomAssignmentRepository,
                          CourseRepository courseRepository) {
        this.userRepository = userRepository;
        this.classroomRepository = classroomRepository;
        this.teacherClassroomAssignmentRepository = teacherClassroomAssignmentRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public void assignTeacherToClassroom(TeacherAssignClassroomRequest request) {
        log.info("Assigning teacher ID: {} to classroom ID: {}", request.getTeacherId(), request.getClassroomId());

        // Check if teacher exists and is actually a teacher
        User teacher = userRepository.findByIdWithDetails(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + request.getTeacherId()));

        if (teacher.getProfileType().getId() != 1) {
            throw new IllegalStateException("User is not a teacher");
        }

        // Check if classroom exists
        Classroom classroom = classroomRepository.findById(request.getClassroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with ID: " + request.getClassroomId()));

        // Check if assignment already exists
        if (teacherClassroomAssignmentRepository.existsByTeacherIdAndClassroomId(request.getTeacherId(), request.getClassroomId())) {
            throw new IllegalStateException("Teacher is already assigned to this classroom");
        }

        // Verify teacher and classroom are in the same organization
        if (!teacher.getOrganization().getId().equals(classroom.getOrganization().getId())) {
            throw new IllegalStateException("Teacher and classroom must be in the same organization");
        }

        TeacherClassroomAssignment assignment = new TeacherClassroomAssignment();
        assignment.setTeacher(teacher);
        assignment.setClassroom(classroom);

        teacherClassroomAssignmentRepository.save(assignment);
        log.info("Teacher assigned successfully: {} to classroom: {}", teacher.getEmail(), classroom.getName());
    }

    @Transactional
    public void unassignTeacherFromClassroom(UUID teacherId, UUID classroomId) {
        log.info("Unassigning teacher ID: {} from classroom ID: {}", teacherId, classroomId);

        TeacherClassroomAssignment assignment = teacherClassroomAssignmentRepository
                .findByTeacherIdAndClassroomId(teacherId, classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher assignment not found"));

        teacherClassroomAssignmentRepository.delete(assignment);
        log.info("Teacher unassigned successfully from classroom");
    }

    public List<TeacherClassroomResponse> getTeacherClassrooms(UUID teacherId) {
        log.info("Fetching classrooms for teacher ID: {}", teacherId);

        // Verify teacher exists and is actually a teacher
        User teacher = userRepository.findByIdWithDetails(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + teacherId));

        if (teacher.getProfileType().getId() != 1) {
            throw new IllegalStateException("User is not a teacher");
        }

        List<Classroom> classrooms = teacherClassroomAssignmentRepository.findClassroomsByTeacherId(teacherId);

        return classrooms.stream()
                .map(this::mapToClassroomResponse)
                .collect(Collectors.toList());
    }

    public List<TeacherStudentResponse> getTeacherStudents(UUID teacherId) {
        log.info("Fetching students for teacher ID: {}", teacherId);

        // Verify teacher exists and is actually a teacher
        User teacher = userRepository.findByIdWithDetails(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + teacherId));

        if (teacher.getProfileType().getId() != 1) {
            throw new IllegalStateException("User is not a teacher");
        }

        List<User> students = userRepository.findStudentsByTeacherId(teacherId);

        return students.stream()
                .map(this::mapToStudentResponse)
                .collect(Collectors.toList());
    }

    public List<TeacherCourseResponse> getTeacherCourses(UUID teacherId) {
        log.info("Fetching courses for teacher ID: {}", teacherId);

        // Verify teacher exists and is actually a teacher
        User teacher = userRepository.findByIdWithDetails(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + teacherId));

        if (teacher.getProfileType().getId() != 1) {
            throw new IllegalStateException("User is not a teacher");
        }

        List<Course> courses = courseRepository.findByTeacherId(teacherId);

        return courses.stream()
                .map(course -> mapToCourseResponse(course, teacherId))
                .collect(Collectors.toList());
    }

    public List<TeacherStudentResponse> getStudentsByTeacherAndClassroom(UUID teacherId, UUID classroomId) {
        log.info("Fetching students for teacher ID: {} in classroom ID: {}", teacherId, classroomId);

        // Verify teacher is assigned to the classroom
        if (!teacherClassroomAssignmentRepository.existsByTeacherIdAndClassroomId(teacherId, classroomId)) {
            throw new IllegalStateException("Teacher is not assigned to this classroom");
        }

        List<User> students = userRepository.findByClassroomId(classroomId);

        return students.stream()
                .filter(user -> user.getProfileType().getId() == 2) // Only students
                .map(this::mapToStudentResponse)
                .collect(Collectors.toList());
    }

    private TeacherClassroomResponse mapToClassroomResponse(Classroom classroom) {
        long studentCount = classroomRepository.countStudentsByClassroomId(classroom.getId());
        long courseCount = classroomRepository.countCoursesByClassroomId(classroom.getId());

        return new TeacherClassroomResponse(
                classroom.getId(),
                classroom.getName(),
                classroom.getOrganization().getId(),
                classroom.getOrganization().getName(),
                (int) studentCount,
                (int) courseCount
        );
    }

    private TeacherStudentResponse mapToStudentResponse(User student) {
        return new TeacherStudentResponse(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                student.getClassroom() != null ? student.getClassroom().getId() : null,
                student.getClassroom() != null ? student.getClassroom().getName() : null
        );
    }

    private TeacherCourseResponse mapToCourseResponse(Course course, UUID teacherId) {
        // Find the first classroom where this teacher teaches this course
        List<TeacherClassroomAssignment> teacherAssignments =
                teacherClassroomAssignmentRepository.findByTeacherIdWithDetails(teacherId);

        // Get the first classroom that has this course assigned
        for (TeacherClassroomAssignment assignment : teacherAssignments) {
            if (courseRepository.findByClassroomId(assignment.getClassroom().getId()).contains(course)) {
                return new TeacherCourseResponse(
                        course.getId(),
                        course.getName(),
                        course.getImageUrl(),
                        assignment.getClassroom().getId(),
                        assignment.getClassroom().getName()
                );
            }
        }

        // If no specific classroom found, return without classroom info
        return new TeacherCourseResponse(
                course.getId(),
                course.getName(),
                course.getImageUrl(),
                null,
                null
        );
    }
}