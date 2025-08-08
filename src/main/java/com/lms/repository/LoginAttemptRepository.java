package com.lms.repository;

import com.lms.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, UUID> {

    List<LoginAttempt> findByEmailAndTimestampAfterOrderByTimestampDesc(String email, LocalDateTime since);

    List<LoginAttempt> findByIpAddressAndTimestampAfterOrderByTimestampDesc(String ipAddress, LocalDateTime since);

    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.email = :email AND la.success = false AND la.timestamp >= :since")
    long countFailedAttemptsByEmailSince(@Param("email") String email, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.ipAddress = :ipAddress AND la.success = false AND la.timestamp >= :since")
    long countFailedAttemptsByIpSince(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.email = :email AND la.timestamp >= :since")
    long countAttemptsByEmailSince(@Param("email") String email, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.ipAddress = :ipAddress AND la.timestamp >= :since")
    long countAttemptsByIpSince(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);
}
