package com.lms.service;

import com.lms.entity.AuditLog;
import com.lms.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void logActivity(UUID userId, String userEmail, String action, String resourceType, 
                           String resourceId, String details, String ipAddress, String userAgent, 
                           String status, String errorMessage) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(userId);
            auditLog.setUserEmail(userEmail);
            auditLog.setAction(action);
            auditLog.setResourceType(resourceType);
            auditLog.setResourceId(resourceId);
            auditLog.setDetails(details);
            auditLog.setIpAddress(ipAddress);
            auditLog.setUserAgent(userAgent);
            auditLog.setStatus(status);
            auditLog.setErrorMessage(errorMessage);
            auditLog.setTimestamp(LocalDateTime.now());

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} - {} - {}", action, resourceType, resourceId);
        } catch (Exception e) {
            log.error("Failed to create audit log: {}", e.getMessage(), e);
        }
    }

    public void logLogin(UUID userId, String userEmail, String ipAddress, String userAgent, boolean success, String errorMessage) {
        logActivity(userId, userEmail, "LOGIN", "AUTH", null, 
                   "Login attempt", ipAddress, userAgent, 
                   success ? "SUCCESS" : "FAILURE", errorMessage);
    }

    public void logLogout(UUID userId, String userEmail, String ipAddress, String userAgent) {
        logActivity(userId, userEmail, "LOGOUT", "AUTH", null, 
                   "User logout", ipAddress, userAgent, "SUCCESS", null);
    }

    public void logCreate(UUID userId, String userEmail, String resourceType, String resourceId, 
                         String details, String ipAddress, String userAgent) {
        logActivity(userId, userEmail, "CREATE", resourceType, resourceId, 
                   details, ipAddress, userAgent, "SUCCESS", null);
    }

    public void logUpdate(UUID userId, String userEmail, String resourceType, String resourceId, 
                         String details, String ipAddress, String userAgent) {
        logActivity(userId, userEmail, "UPDATE", resourceType, resourceId, 
                   details, ipAddress, userAgent, "SUCCESS", null);
    }

    public void logDelete(UUID userId, String userEmail, String resourceType, String resourceId, 
                         String details, String ipAddress, String userAgent) {
        logActivity(userId, userEmail, "DELETE", resourceType, resourceId, 
                   details, ipAddress, userAgent, "SUCCESS", null);
    }

    public void logRead(UUID userId, String userEmail, String resourceType, String resourceId, 
                       String details, String ipAddress, String userAgent) {
        logActivity(userId, userEmail, "READ", resourceType, resourceId, 
                   details, ipAddress, userAgent, "SUCCESS", null);
    }

    public Page<AuditLog> getUserActivity(UUID userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
    }

    public Page<AuditLog> getResourceActivity(String resourceType, String resourceId, Pageable pageable) {
        return auditLogRepository.findByResourceTypeAndResourceIdOrderByTimestampDesc(resourceType, resourceId, pageable);
    }

    public Page<AuditLog> getActivityByAction(String action, Pageable pageable) {
        return auditLogRepository.findByActionOrderByTimestampDesc(action, pageable);
    }

    public Page<AuditLog> getActivityByTimeRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(start, end, pageable);
    }
}
