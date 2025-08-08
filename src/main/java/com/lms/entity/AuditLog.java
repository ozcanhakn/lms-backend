package com.lms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "action", nullable = false)
    private String action; // CREATE, READ, UPDATE, DELETE, LOGIN, LOGOUT, etc.

    @Column(name = "resource_type", nullable = false)
    private String resourceType; // USER, COURSE, CLASSROOM, etc.

    @Column(name = "resource_id")
    private String resourceId;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details; // JSON string with additional details

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "status")
    private String status; // SUCCESS, FAILURE, ERROR

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
