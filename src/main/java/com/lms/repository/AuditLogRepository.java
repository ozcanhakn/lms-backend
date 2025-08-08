package com.lms.repository;

import com.lms.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findByUserIdOrderByTimestampDesc(UUID userId, Pageable pageable);

    Page<AuditLog> findByResourceTypeAndResourceIdOrderByTimestampDesc(String resourceType, String resourceId, Pageable pageable);

    Page<AuditLog> findByActionOrderByTimestampDesc(String action, Pageable pageable);

    Page<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.userId = :userId AND al.timestamp >= :since ORDER BY al.timestamp DESC")
    List<AuditLog> findUserActivitySince(@Param("userId") UUID userId, @Param("since") LocalDateTime since);

    @Query("SELECT al FROM AuditLog al WHERE al.ipAddress = :ipAddress AND al.timestamp >= :since ORDER BY al.timestamp DESC")
    List<AuditLog> findActivityByIpSince(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.userId = :userId AND al.action = 'LOGIN' AND al.success = false AND al.timestamp >= :since")
    long countFailedLoginAttemptsByUserSince(@Param("userId") UUID userId, @Param("since") LocalDateTime since);
}
