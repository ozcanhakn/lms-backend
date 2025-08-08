package com.lms.repository;

import com.lms.entity.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, UUID> {

    Optional<Classroom> findByName(String name);

    List<Classroom> findByOrganizationId(UUID organizationId);

    boolean existsByName(String name);

    boolean existsByNameAndOrganizationId(String name, UUID organizationId);

    // Ogretmenin atandigi siniflari getirmek icin
    @Query("SELECT c FROM Classroom c " +
            "JOIN c.teacherAssignments ta " +
            "WHERE ta.teacher.id = :teacherId")
    List<Classroom> findByTeacherId(@Param("teacherId") UUID teacherId);

    // Organization ile birlikte fetch etmek için
    @Query("SELECT c FROM Classroom c JOIN FETCH c.organization WHERE c.id = :id")
    Optional<Classroom> findByIdWithOrganization(@Param("id") UUID id);

    // Classroom'daki ogrenci sayisini getirmek icin
    @Query("SELECT COUNT(u) FROM User u WHERE u.classroom.id = :classroomId")
    long countStudentsByClassroomId(@Param("classroomId") UUID classroomId);

    // Classroom'a atanmis ders sayisini getirmek icin
    @Query("SELECT COUNT(ca) FROM CourseAssignment ca WHERE ca.classroom.id = :classroomId")
    long countCoursesByClassroomId(@Param("classroomId") UUID classroomId);
}