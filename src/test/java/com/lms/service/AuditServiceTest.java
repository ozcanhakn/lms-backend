package com.lms.service;

import com.lms.entity.AuditLog;
import com.lms.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    private UUID userId;
    private String userEmail;
    private String ipAddress;
    private String userAgent;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userEmail = "test@example.com";
        ipAddress = "192.168.1.1";
        userAgent = "Mozilla/5.0";
    }

    @Test
    void testLogActivity() {
        // Given
        String action = "CREATE";
        String resourceType = "USER";
        String resourceId = "123";
        String details = "User created";
        String status = "SUCCESS";
        String errorMessage = null;

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // When
        auditService.logActivity(userId, userEmail, action, resourceType, resourceId, 
                               details, ipAddress, userAgent, status, errorMessage);

        // Then
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testLogLogin() {
        // Given
        boolean success = true;
        String errorMessage = null;

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // When
        auditService.logLogin(userId, userEmail, ipAddress, userAgent, success, errorMessage);

        // Then
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testLogLoginFailure() {
        // Given
        boolean success = false;
        String errorMessage = "Invalid credentials";

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // When
        auditService.logLogin(userId, userEmail, ipAddress, userAgent, success, errorMessage);

        // Then
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testLogLogout() {
        // Given
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // When
        auditService.logLogout(userId, userEmail, ipAddress, userAgent);

        // Then
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testLogCreate() {
        // Given
        String resourceType = "COURSE";
        String resourceId = "456";
        String details = "Course created";

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // When
        auditService.logCreate(userId, userEmail, resourceType, resourceId, details, ipAddress, userAgent);

        // Then
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testLogUpdate() {
        // Given
        String resourceType = "USER";
        String resourceId = "789";
        String details = "User updated";

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // When
        auditService.logUpdate(userId, userEmail, resourceType, resourceId, details, ipAddress, userAgent);

        // Then
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testLogDelete() {
        // Given
        String resourceType = "CLASSROOM";
        String resourceId = "101";
        String details = "Classroom deleted";

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // When
        auditService.logDelete(userId, userEmail, resourceType, resourceId, details, ipAddress, userAgent);

        // Then
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testLogRead() {
        // Given
        String resourceType = "ORGANIZATION";
        String resourceId = "202";
        String details = "Organization read";

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // When
        auditService.logRead(userId, userEmail, resourceType, resourceId, details, ipAddress, userAgent);

        // Then
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testGetUserActivity() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<AuditLog> auditLogs = List.of(new AuditLog(), new AuditLog());
        Page<AuditLog> expectedPage = new PageImpl<>(auditLogs, pageable, auditLogs.size());

        when(auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable)).thenReturn(expectedPage);

        // When
        Page<AuditLog> result = auditService.getUserActivity(userId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(auditLogRepository, times(1)).findByUserIdOrderByTimestampDesc(userId, pageable);
    }

    @Test
    void testGetResourceActivity() {
        // Given
        String resourceType = "USER";
        String resourceId = "123";
        Pageable pageable = PageRequest.of(0, 10);
        List<AuditLog> auditLogs = List.of(new AuditLog());
        Page<AuditLog> expectedPage = new PageImpl<>(auditLogs, pageable, auditLogs.size());

        when(auditLogRepository.findByResourceTypeAndResourceIdOrderByTimestampDesc(resourceType, resourceId, pageable))
            .thenReturn(expectedPage);

        // When
        Page<AuditLog> result = auditService.getResourceActivity(resourceType, resourceId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(auditLogRepository, times(1))
            .findByResourceTypeAndResourceIdOrderByTimestampDesc(resourceType, resourceId, pageable);
    }

    @Test
    void testGetActivityByAction() {
        // Given
        String action = "CREATE";
        Pageable pageable = PageRequest.of(0, 10);
        List<AuditLog> auditLogs = List.of(new AuditLog(), new AuditLog(), new AuditLog());
        Page<AuditLog> expectedPage = new PageImpl<>(auditLogs, pageable, auditLogs.size());

        when(auditLogRepository.findByActionOrderByTimestampDesc(action, pageable)).thenReturn(expectedPage);

        // When
        Page<AuditLog> result = auditService.getActivityByAction(action, pageable);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        verify(auditLogRepository, times(1)).findByActionOrderByTimestampDesc(action, pageable);
    }

    @Test
    void testGetActivityByTimeRange() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);
        List<AuditLog> auditLogs = List.of(new AuditLog());
        Page<AuditLog> expectedPage = new PageImpl<>(auditLogs, pageable, auditLogs.size());

        when(auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(start, end, pageable))
            .thenReturn(expectedPage);

        // When
        Page<AuditLog> result = auditService.getActivityByTimeRange(start, end, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(auditLogRepository, times(1))
            .findByTimestampBetweenOrderByTimestampDesc(start, end, pageable);
    }
}
