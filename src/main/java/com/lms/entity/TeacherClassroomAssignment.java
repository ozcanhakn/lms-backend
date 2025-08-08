package com.lms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "teacher_classroom_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherClassroomAssignment {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    // Unique constraint to prevent duplicate assignments
    @Table(uniqueConstraints = @jakarta.persistence.UniqueConstraint(columnNames = {"teacher_id", "classroom_id"}))
    public static class UniqueConstraint {}
}