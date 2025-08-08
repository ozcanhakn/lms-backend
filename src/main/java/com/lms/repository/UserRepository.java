package com.lms.repository;

import com.lms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Profile type'a göre kullanıcıları getirmek için
    List<User> findByProfileTypeId(Integer profileTypeId);

    // Organization'a göre kullanıcıları getirmek için
    List<User> findByOrganizationId(UUID organizationId);

    // Sadece öğretmenleri getirmek için
    @Query("SELECT u FROM User u WHERE u.profileType.id = 1")
    List<User> findAllTeachers();

    // Sadece öğrencileri getirmek için
    @Query("SELECT u FROM User u WHERE u.profileType.id = 2")
    List<User> findAllStudents();

    // Belirli sınıftaki öğrencileri getirmek için
    List<User> findByClassroomId(UUID classroomId);

    // Öğretmenin atandığı sınıflardaki öğrencileri getirmek için
    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN u.classroom c " +
            "JOIN c.teacherAssignments ta " +
            "WHERE ta.teacher.id = :teacherId AND u.profileType.id = 2")
    List<User> findStudentsByTeacherId(@Param("teacherId") UUID teacherId);

    // User'ı tüm ilişkileriyle birlikte getirmek için
    @Query("SELECT u FROM User u " +
            "JOIN FETCH u.profileType " +
            "LEFT JOIN FETCH u.organization " +
            "LEFT JOIN FETCH u.classroom " +
            "WHERE u.id = :id")
    Optional<User> findByIdWithDetails(@Param("id") UUID id);

    // Email ile user'ı tüm ilişkileriyle birlikte getirmek için (authentication için)
    @Query("SELECT u FROM User u " +
            "JOIN FETCH u.profileType " +
            "LEFT JOIN FETCH u.organization " +
            "LEFT JOIN FETCH u.classroom " +
            "WHERE u.email = :email")
    Optional<User> findByEmailWithDetails(@Param("email") String email);

    // Organizasyon ve profile type'a göre kullanıcıları getirmek için
    List<User> findByOrganizationIdAndProfileTypeId(UUID organizationId, Integer profileTypeId);
}