package com.lms.service;

import com.lms.dto.classroom.ClassroomRequest;
import com.lms.dto.classroom.ClassroomResponse;
import com.lms.entity.Classroom;
import com.lms.entity.Organization;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.ClassroomRepository;
import com.lms.repository.OrganizationRepository;
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
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final OrganizationRepository organizationRepository;

    public ClassroomService(ClassroomRepository classroomRepository, OrganizationRepository organizationRepository) {
        this.classroomRepository = classroomRepository;
        this.organizationRepository = organizationRepository;
    }

    @Transactional
    public ClassroomResponse createClassroom(ClassroomRequest request) {
        log.info("Creating new classroom with name: {} for organization ID: {}", request.getName(), request.getOrganizationId());

        // Check if organization exists
        Organization organization = organizationRepository.findByIdWithBrand(request.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + request.getOrganizationId()));

        // Check if classroom with same name and organization already exists
        if (classroomRepository.existsByNameAndOrganizationId(request.getName(), request.getOrganizationId())) {
            throw new IllegalStateException("Classroom with name '" + request.getName() + "' already exists in this organization");
        }

        Classroom classroom = new Classroom();
        classroom.setName(request.getName());
        classroom.setOrganization(organization);

        Classroom savedClassroom = classroomRepository.save(classroom);
        log.info("Classroom created successfully: {}", savedClassroom.getName());

        return mapToResponse(savedClassroom);
    }

    public List<ClassroomResponse> getAllClassrooms() {
        log.info("Fetching all classrooms");
        List<Classroom> classrooms = classroomRepository.findAll();
        return classrooms.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ClassroomResponse getClassroomById(UUID id) {
        log.info("Fetching classroom with ID: {}", id);
        Classroom classroom = classroomRepository.findByIdWithOrganization(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with ID: " + id));

        return mapToResponse(classroom);
    }

    @Transactional
    public ClassroomResponse updateClassroom(UUID id, ClassroomRequest request) {
        log.info("Updating classroom with ID: {}", id);

        Classroom classroom = classroomRepository.findByIdWithOrganization(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with ID: " + id));

        // Check if organization exists
        Organization organization = organizationRepository.findByIdWithBrand(request.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + request.getOrganizationId()));

        // Check if another classroom with same name and organization exists
        if (classroomRepository.existsByNameAndOrganizationId(request.getName(), request.getOrganizationId()) &&
                (!classroom.getName().equals(request.getName()) || !classroom.getOrganization().getId().equals(request.getOrganizationId()))) {
            throw new IllegalStateException("Classroom with name '" + request.getName() + "' already exists in this organization");
        }

        classroom.setName(request.getName());
        classroom.setOrganization(organization);

        Classroom savedClassroom = classroomRepository.save(classroom);
        log.info("Classroom updated successfully: {}", savedClassroom.getName());

        return mapToResponse(savedClassroom);
    }

    @Transactional
    public void deleteClassroom(UUID id) {
        log.info("Attempting to delete classroom with ID: {}", id);

        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with ID: " + id));

        // Check if classroom has students
        long studentCount = classroomRepository.countStudentsByClassroomId(id);
        if (studentCount > 0) {
            throw new IllegalStateException("Cannot delete classroom. It has " + studentCount + " students. Please reassign students first.");
        }

        // Check if classroom has course assignments
        long courseCount = classroomRepository.countCoursesByClassroomId(id);
        if (courseCount > 0) {
            throw new IllegalStateException("Cannot delete classroom. It has " + courseCount + " course assignments. Please remove course assignments first.");
        }

        try {
            classroomRepository.delete(classroom);
            log.info("Classroom deleted successfully: {}", classroom.getName());
        } catch (DataIntegrityViolationException e) {
            log.error("Failed to delete classroom due to foreign key constraints: {}", e.getMessage());
            throw new IllegalStateException("Cannot delete classroom. It has associated records.");
        }
    }

    public List<ClassroomResponse> getClassroomsByOrganization(UUID organizationId) {
        log.info("Fetching classrooms for organization ID: {}", organizationId);

        if (!organizationRepository.existsById(organizationId)) {
            throw new ResourceNotFoundException("Organization not found with ID: " + organizationId);
        }

        List<Classroom> classrooms = classroomRepository.findByOrganizationId(organizationId);
        return classrooms.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ClassroomResponse> getClassroomsByTeacher(UUID teacherId) {
        log.info("Fetching classrooms for teacher ID: {}", teacherId);

        List<Classroom> classrooms = classroomRepository.findByTeacherId(teacherId);
        return classrooms.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ClassroomResponse mapToResponse(Classroom classroom) {
        long studentCount = classroomRepository.countStudentsByClassroomId(classroom.getId());
        long courseCount = classroomRepository.countCoursesByClassroomId(classroom.getId());

        // Count teachers assigned to this classroom
        int teacherCount = classroom.getTeacherAssignments() != null ? classroom.getTeacherAssignments().size() : 0;

        return new ClassroomResponse(
                classroom.getId(),
                classroom.getName(),
                classroom.getOrganization().getId(),
                classroom.getOrganization().getName(),
                classroom.getOrganization().getBrand().getName(),
                (int) studentCount,
                (int) courseCount,
                teacherCount
        );
    }
}