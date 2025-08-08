package com.lms.repository;

import com.lms.entity.CourseAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseAssignmentRepository extends JpaRepository<CourseAssignment, UUID> {

    // Belirli bir course ve classroom kombinasyonu var mı kontrolü
    boolean existsByCourseIdAndClassroomId(UUID courseId, UUID classroomId);

    // Course ve classroom kombinasyonunu getirmek icin
    Optional<CourseAssignment> findByCourseIdAndClassroomId(UUID courseId, UUID classroomId);

    // Belirli bir course'un tüm atamalarını getirmek için
    List<CourseAssignment> findByCourseId(UUID courseId);

    // Belirli bir classroom'un tüm ders atamalarını getirmek için
    List<CourseAssignment> findByClassroomId(UUID classroomId);

    // Course ve classroom bilgileriyle birlikte getirmek için
    @Query("SELECT ca FROM CourseAssignment ca " +
            "JOIN FETCH ca.course " +
            "JOIN FETCH ca.classroom " +
            "WHERE ca.id = :id")
    Optional<CourseAssignment> findByIdWithDetails(@Param("id") UUID id);

    // Belirli bir classroom'daki tüm ders atamalarını detaylarıyla getirmek için
    @Query("SELECT ca FROM CourseAssignment ca " +
            "JOIN FETCH ca.course " +
            "JOIN FETCH ca.classroom " +
            "WHERE ca.classroom.id = :classroomId")
    List<CourseAssignment> findByClassroomIdWithDetails(@Param("classroomId") UUID classroomId);

    // Belirli bir course'un tüm atamalarını detaylarıyla getirmek için
    @Query("SELECT ca FROM CourseAssignment ca " +
            "JOIN FETCH ca.course " +
            "JOIN FETCH ca.classroom " +
            "WHERE ca.course.id = :courseId")
    List<CourseAssignment> findByCourseIdWithDetails(@Param("courseId") UUID courseId);
}