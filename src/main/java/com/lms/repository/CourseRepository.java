package com.lms.repository;

import com.lms.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {

    Optional<Course> findByName(String name);

    boolean existsByName(String name);

    // Öğrencinin sınıfına atanmış dersleri getirmek için
    @Query("SELECT c FROM Course c " +
            "JOIN c.courseAssignments ca " +
            "WHERE ca.classroom.id = :classroomId")
    List<Course> findByClassroomId(@Param("classroomId") UUID classroomId);

    // Öğretmenin atandığı sınıflardaki dersleri getirmek için
    @Query("SELECT DISTINCT c FROM Course c " +
            "JOIN c.courseAssignments ca " +
            "JOIN ca.classroom.teacherAssignments ta " +
            "WHERE ta.teacher.id = :teacherId")
    List<Course> findByTeacherId(@Param("teacherId") UUID teacherId);

    // Belirli bir classroom'a atanmamış dersleri getirmek için
    @Query("SELECT c FROM Course c " +
            "WHERE c.id NOT IN (" +
            "    SELECT ca.course.id FROM CourseAssignment ca " +
            "    WHERE ca.classroom.id = :classroomId" +
            ")")
    List<Course> findNotAssignedToClassroom(@Param("classroomId") UUID classroomId);
}