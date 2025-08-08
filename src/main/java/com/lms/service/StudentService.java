package com.lms.service;

import com.lms.dto.student.StudentCourseResponse;
import com.lms.entity.Course;
import com.lms.entity.User;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.CourseRepository;
import com.lms.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class StudentService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public StudentService(UserRepository userRepository, CourseRepository courseRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    public List<StudentCourseResponse> getStudentCourses(UUID studentId) {
        log.info("Fetching courses for student ID: {}", studentId);

        // ogrenci varligi ve gercekten ogrenci oldugunu dogrulama
        User student = userRepository.findByIdWithDetails(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        if (student.getProfileType().getId() != 2) {
            throw new IllegalStateException("User is not a student");
        }

        // ogrencinin bir sinifa atanip atanmadigi kontrolu
        if (student.getClassroom() == null) {
            throw new IllegalStateException("Student is not assigned to any classroom");
        }

        List<Course> courses = courseRepository.findByClassroomId(student.getClassroom().getId());

        return courses.stream()
                .map(course -> mapToCourseResponse(course, student))
                .collect(Collectors.toList());
    }

    public List<StudentCourseResponse> getCoursesByClassroom(UUID classroomId) {
        log.info("Fetching courses for classroom ID: {}", classroomId);

        List<Course> courses = courseRepository.findByClassroomId(classroomId);

        return courses.stream()
                .map(course -> new StudentCourseResponse(
                        course.getId(),
                        course.getName(),
                        course.getImageUrl(),
                        "Classroom " + classroomId
                ))
                .collect(Collectors.toList());
    }

    private StudentCourseResponse mapToCourseResponse(Course course, User student) {
        return new StudentCourseResponse(
                course.getId(),
                course.getName(),
                course.getImageUrl(),
                student.getClassroom().getName()
        );
    }
}