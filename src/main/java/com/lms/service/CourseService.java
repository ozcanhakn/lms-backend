package com.lms.service;

import com.lms.dto.course.CourseAssignRequest;
import com.lms.dto.course.CourseRequest;
import com.lms.dto.course.CourseResponse;
import com.lms.entity.Classroom;
import com.lms.entity.Course;
import com.lms.entity.CourseAssignment;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.ClassroomRepository;
import com.lms.repository.CourseAssignmentRepository;
import com.lms.repository.CourseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final ClassroomRepository classroomRepository;
    private final CourseAssignmentRepository courseAssignmentRepository;

    public CourseService(CourseRepository courseRepository,
                         ClassroomRepository classroomRepository,
                         CourseAssignmentRepository courseAssignmentRepository) {
        this.courseRepository = courseRepository;
        this.classroomRepository = classroomRepository;
        this.courseAssignmentRepository = courseAssignmentRepository;
    }

    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        log.info("Creating new course with name: {}", request.getName());

        // Check if course with same name already exists
        if (courseRepository.existsByName(request.getName())) {
            throw new IllegalStateException("Course with name '" + request.getName() + "' already exists");
        }

        Course course = new Course();
        course.setName(request.getName());
        course.setImageUrl(request.getImageUrl());

        Course savedCourse = courseRepository.save(course);
        log.info("Course created successfully: {}", savedCourse.getName());

        return mapToResponse(savedCourse);
    }

    public List<CourseResponse> getAllCourses() {
        log.info("Fetching all courses");
        List<Course> courses = courseRepository.findAll();
        return courses.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CourseResponse getCourseById(UUID id) {
        log.info("Fetching course with ID: {}", id);
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + id));

        return mapToResponse(course);
    }

    @Transactional
    public CourseResponse updateCourse(UUID id, CourseRequest request) {
        log.info("Updating course with ID: {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + id));

        // Check if another course with same name exists
        if (!course.getName().equals(request.getName()) && courseRepository.existsByName(request.getName())) {
            throw new IllegalStateException("Course with name '" + request.getName() + "' already exists");
        }

        course.setName(request.getName());
        course.setImageUrl(request.getImageUrl());

        Course savedCourse = courseRepository.save(course);
        log.info("Course updated successfully: {}", savedCourse.getName());

        return mapToResponse(savedCourse);
    }

    @Transactional
    public void deleteCourse(UUID id) {
        log.info("Attempting to delete course with ID: {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + id));

        try {
            courseRepository.delete(course);
            log.info("Course deleted successfully: {}", course.getName());
        } catch (DataIntegrityViolationException e) {
            log.error("Failed to delete course due to foreign key constraints: {}", e.getMessage());
            throw new IllegalStateException("Cannot delete course. It has associated assignments. Please remove all course assignments first.");
        }
    }

    @Transactional
    public void assignCourseToClassroom(CourseAssignRequest request) {
        log.info("Assigning course ID: {} to classroom ID: {}", request.getCourseId(), request.getClassroomId());

        // Check if course exists
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + request.getCourseId()));

        // Check if classroom exists
        Classroom classroom = classroomRepository.findById(request.getClassroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with ID: " + request.getClassroomId()));

        // Check if assignment already exists
        if (courseAssignmentRepository.existsByCourseIdAndClassroomId(request.getCourseId(), request.getClassroomId())) {
            throw new IllegalStateException("Course is already assigned to this classroom");
        }

        CourseAssignment assignment = new CourseAssignment();
        assignment.setCourse(course);
        assignment.setClassroom(classroom);

        courseAssignmentRepository.save(assignment);
        log.info("Course assigned successfully: {} to classroom: {}", course.getName(), classroom.getName());
    }

    @Transactional
    public void unassignCourseFromClassroom(UUID courseId, UUID classroomId) {
        log.info("Unassigning course ID: {} from classroom ID: {}", courseId, classroomId);

        CourseAssignment assignment = courseAssignmentRepository.findByCourseIdAndClassroomId(courseId, classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Course assignment not found"));

        courseAssignmentRepository.delete(assignment);
        log.info("Course unassigned successfully from classroom");
    }

    public List<CourseResponse> getCoursesByClassroom(UUID classroomId) {
        log.info("Fetching courses for classroom ID: {}", classroomId);

        if (!classroomRepository.existsById(classroomId)) {
            throw new ResourceNotFoundException("Classroom not found with ID: " + classroomId);
        }

        List<Course> courses = courseRepository.findByClassroomId(classroomId);
        return courses.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<CourseResponse> getCoursesByTeacher(UUID teacherId) {
        log.info("Fetching courses for teacher ID: {}", teacherId);

        List<Course> courses = courseRepository.findByTeacherId(teacherId);
        return courses.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<CourseResponse> getAvailableCoursesForClassroom(UUID classroomId) {
        log.info("Fetching available courses for classroom ID: {}", classroomId);

        if (!classroomRepository.existsById(classroomId)) {
            throw new ResourceNotFoundException("Classroom not found with ID: " + classroomId);
        }

        List<Course> courses = courseRepository.findNotAssignedToClassroom(classroomId);
        return courses.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CourseResponse mapToResponse(Course course) {
        int assignedClassroomCount = course.getCourseAssignments() != null ? course.getCourseAssignments().size() : 0;

        return new CourseResponse(
                course.getId(),
                course.getName(),
                course.getImageUrl(),
                assignedClassroomCount
        );
    }
}