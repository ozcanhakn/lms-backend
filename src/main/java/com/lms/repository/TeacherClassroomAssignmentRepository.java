package com.lms.repository;

import com.lms.entity.TeacherClassroomAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeacherClassroomAssignmentRepository extends JpaRepository<TeacherClassroomAssignment, UUID> {

    // Belirli bir teacher ve classroom kombinasyonu var mı kontrolü
    boolean existsByTeacherIdAndClassroomId(UUID teacherId, UUID classroomId);

    // Teacher ve classroom kombinasyonunu getirmek için
    Optional<TeacherClassroomAssignment> findByTeacherIdAndClassroomId(UUID teacherId, UUID classroomId);

    // Belirli bir teacher'ın tüm sınıf atamalarını getirmek için
    List<TeacherClassroomAssignment> findByTeacherId(UUID teacherId);

    // Belirli bir classroom'un tüm öğretmen atamalarını getirmek için
    List<TeacherClassroomAssignment> findByClassroomId(UUID classroomId);

    // Teacher ve classroom bilgileriyle birlikte getirmek için
    @Query("SELECT tca FROM TeacherClassroomAssignment tca " +
            "JOIN FETCH tca.teacher " +
            "JOIN FETCH tca.classroom " +
            "WHERE tca.id = :id")
    Optional<TeacherClassroomAssignment> findByIdWithDetails(@Param("id") UUID id);

    // Belirli bir teacher'ın tüm sınıf atamalarını detaylarıyla getirmek için
    @Query("SELECT tca FROM TeacherClassroomAssignment tca " +
            "JOIN FETCH tca.teacher " +
            "JOIN FETCH tca.classroom " +
            "WHERE tca.teacher.id = :teacherId")
    List<TeacherClassroomAssignment> findByTeacherIdWithDetails(@Param("teacherId") UUID teacherId);

    // Belirli bir classroom'un tüm öğretmen atamalarını detaylarıyla getirmek için
    @Query("SELECT tca FROM TeacherClassroomAssignment tca " +
            "JOIN FETCH tca.teacher " +
            "JOIN FETCH tca.classroom " +
            "WHERE tca.classroom.id = :classroomId")
    List<TeacherClassroomAssignment> findByClassroomIdWithDetails(@Param("classroomId") UUID classroomId);

    // Öğretmenin sınıflarını direct olarak getirmek için
    @Query("SELECT tca.classroom FROM TeacherClassroomAssignment tca WHERE tca.teacher.id = :teacherId")
    List<com.lms.entity.Classroom> findClassroomsByTeacherId(@Param("teacherId") UUID teacherId);
}