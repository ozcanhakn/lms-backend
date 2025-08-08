package com.lms.service;

import com.lms.dto.user.CreateUserRequest;
import com.lms.dto.user.UserResponse;
import com.lms.entity.*;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final ClassroomRepository classroomRepository;
    private final ProfileTypeRepository profileTypeRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       OrganizationRepository organizationRepository,
                       ClassroomRepository classroomRepository,
                       ProfileTypeRepository profileTypeRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.classroomRepository = classroomRepository;
        this.profileTypeRepository = profileTypeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating new user with email: {} and profile ID: {}", request.getEmail(), request.getProfileId());

        // Check if user with same email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("User with email '" + request.getEmail() + "' already exists");
        }

        // Validate profile type (only Teacher=1 or Student=2 can be created via API)
        if (request.getProfileId() != 1 && request.getProfileId() != 2) {
            throw new IllegalStateException("Invalid profile ID. Only Teachers (1) or Students (2) can be created via API");
        }

        // Check if organization exists
        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + request.getOrganizationId()));

        // Check if profile type exists
        ProfileType profileType = profileTypeRepository.findById(request.getProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile type not found with ID: " + request.getProfileId()));

        Classroom classroom = null;
        // If creating a student, classroom is required
        if (request.getProfileId() == 2) { // Student
            if (request.getClassroomId() == null) {
                throw new IllegalStateException("Classroom ID is required for students");
            }
            classroom = classroomRepository.findById(request.getClassroomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with ID: " + request.getClassroomId()));

            // Verify classroom belongs to the same organization
            if (!classroom.getOrganization().getId().equals(organization.getId())) {
                throw new IllegalStateException("Classroom does not belong to the specified organization");
            }
        } else if (request.getClassroomId() != null) {
            // Teachers should not have classroom_id
            throw new IllegalStateException("Teachers cannot be assigned to a classroom during creation");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setProfileType(profileType);
        user.setOrganization(organization);
        user.setClassroom(classroom); // ogretmenler icin bos, ogrenciler icin atanmis

        User savedUser = userRepository.save(user);
        log.info("User created successfully: {} with profile: {}", savedUser.getEmail(), profileType.getName());

        return mapToResponse(savedUser);
    }

    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users");
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(UUID id) {
        log.info("Fetching user with ID: {}", id);
        User user = userRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        return mapToResponse(user);
    }

    @Transactional
    public UserResponse updateUser(UUID id, CreateUserRequest request) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // Check if another user with same email exists
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("User with email '" + request.getEmail() + "' already exists");
        }

        // Validate profile type
        if (request.getProfileId() != 1 && request.getProfileId() != 2) {
            throw new IllegalStateException("Invalid profile ID. Only Teachers (1) or Students (2) can be updated via API");
        }

        // Check if organization exists
        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + request.getOrganizationId()));

        // Check if profile type exists
        ProfileType profileType = profileTypeRepository.findById(request.getProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile type not found with ID: " + request.getProfileId()));

        Classroom classroom = null;
        if (request.getProfileId() == 2) { // Student
            if (request.getClassroomId() == null) {
                throw new IllegalStateException("Classroom ID is required for students");
            }
            classroom = classroomRepository.findById(request.getClassroomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Classroom not found with ID: " + request.getClassroomId()));

            if (!classroom.getOrganization().getId().equals(organization.getId())) {
                throw new IllegalStateException("Classroom does not belong to the specified organization");
            }
        } else if (request.getClassroomId() != null) {
            throw new IllegalStateException("Teachers cannot be assigned to a classroom");
        }

        user.setEmail(request.getEmail());
        if (!request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setProfileType(profileType);
        user.setOrganization(organization);
        user.setClassroom(classroom);

        User savedUser = userRepository.save(user);
        log.info("User updated successfully: {}", savedUser.getEmail());

        return mapToResponse(savedUser);
    }

    @Transactional
    public void deleteUser(UUID id) {
        log.info("Attempting to delete user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // superadmin silinemez
        if (user.getProfileType().getId() == 0) {
            throw new IllegalStateException("SuperAdmin users cannot be deleted");
        }

        userRepository.delete(user);
        log.info("User deleted successfully: {}", user.getEmail());
    }

    public List<UserResponse> getUsersByOrganization(UUID organizationId) {
        log.info("Fetching users for organization ID: {}", organizationId);

        if (!organizationRepository.existsById(organizationId)) {
            throw new ResourceNotFoundException("Organization not found with ID: " + organizationId);
        }

        List<User> users = userRepository.findByOrganizationId(organizationId);
        return users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getTeachers() {
        log.info("Fetching all teachers");
        List<User> teachers = userRepository.findAllTeachers();
        return teachers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getStudents() {
        log.info("Fetching all students");
        List<User> students = userRepository.findAllStudents();
        return students.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getStudentsByClassroom(UUID classroomId) {
        log.info("Fetching students for classroom ID: {}", classroomId);

        if (!classroomRepository.existsById(classroomId)) {
            throw new ResourceNotFoundException("Classroom not found with ID: " + classroomId);
        }

        List<User> students = userRepository.findByClassroomId(classroomId);
        return students.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getProfileType().getId(),
                user.getProfileType().getName(),
                user.getOrganization() != null ? user.getOrganization().getId() : null,
                user.getOrganization() != null ? user.getOrganization().getName() : null,
                user.getClassroom() != null ? user.getClassroom().getId() : null,
                user.getClassroom() != null ? user.getClassroom().getName() : null
        );
    }
}