package com.lms.service;

import com.lms.entity.LoginAttempt;
import com.lms.repository.LoginAttemptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    @Mock
    private LoginAttemptRepository loginAttemptRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private RateLimitService rateLimitService;

    private String email;
    private String ipAddress;
    private String userAgent;

    @BeforeEach
    void setUp() {
        email = "test@example.com";
        ipAddress = "192.168.1.1";
        userAgent = "Mozilla/5.0";
        
        // Set default values for rate limiting
        ReflectionTestUtils.setField(rateLimitService, "maxLoginAttempts", 5);
        ReflectionTestUtils.setField(rateLimitService, "loginWindowMinutes", 15);
        ReflectionTestUtils.setField(rateLimitService, "maxIpAttempts", 10);
        ReflectionTestUtils.setField(rateLimitService, "ipWindowMinutes", 15);
    }

    @Test
    void testIsLoginAllowed_FirstAttempt() {
        // Given
        when(loginAttemptRepository.save(any(LoginAttempt.class))).thenReturn(new LoginAttempt());

        // When
        boolean result = rateLimitService.isLoginAllowed(email, ipAddress);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsLoginAllowed_WithinLimit() {
        // Given
        when(loginAttemptRepository.save(any(LoginAttempt.class))).thenReturn(new LoginAttempt());

        // When - Make multiple attempts within limit
        boolean result1 = rateLimitService.isLoginAllowed(email, ipAddress);
        boolean result2 = rateLimitService.isLoginAllowed(email, ipAddress);
        boolean result3 = rateLimitService.isLoginAllowed(email, ipAddress);

        // Then
        assertTrue(result1);
        assertTrue(result2);
        assertTrue(result3);
    }

    @Test
    void testIsLoginAllowed_ExceedsLimit() {
        // Given
        when(loginAttemptRepository.save(any(LoginAttempt.class))).thenReturn(new LoginAttempt());

        // When - Make more attempts than allowed
        boolean[] results = new boolean[6];
        for (int i = 0; i < 6; i++) {
            results[i] = rateLimitService.isLoginAllowed(email, ipAddress);
        }

        // Then - First 5 should be allowed, 6th should be blocked
        assertTrue(results[0]);
        assertTrue(results[1]);
        assertTrue(results[2]);
        assertTrue(results[3]);
        assertTrue(results[4]);
        assertFalse(results[5]);
    }

    @Test
    void testRecordLoginAttempt_Success() {
        // Given
        boolean success = true;
        String failureReason = null;
        when(loginAttemptRepository.save(any(LoginAttempt.class))).thenReturn(new LoginAttempt());

        // When
        rateLimitService.recordLoginAttempt(email, ipAddress, userAgent, success, failureReason);

        // Then
        verify(loginAttemptRepository, times(1)).save(any(LoginAttempt.class));
        verify(auditService, times(1)).logLogin(null, email, ipAddress, userAgent, success, failureReason);
    }

    @Test
    void testRecordLoginAttempt_Failure() {
        // Given
        boolean success = false;
        String failureReason = "Invalid credentials";
        when(loginAttemptRepository.save(any(LoginAttempt.class))).thenReturn(new LoginAttempt());

        // When
        rateLimitService.recordLoginAttempt(email, ipAddress, userAgent, success, failureReason);

        // Then
        verify(loginAttemptRepository, times(1)).save(any(LoginAttempt.class));
        verify(auditService, times(1)).logLogin(null, email, ipAddress, userAgent, success, failureReason);
    }

    @Test
    void testGetFailedAttemptsByEmail() {
        // Given
        LocalDateTime since = LocalDateTime.now().minusMinutes(15);
        long expectedCount = 3L;
        when(loginAttemptRepository.countFailedAttemptsByEmailSince(email, since)).thenReturn(expectedCount);

        // When
        long result = rateLimitService.getFailedAttemptsByEmail(email, since);

        // Then
        assertEquals(expectedCount, result);
        verify(loginAttemptRepository, times(1)).countFailedAttemptsByEmailSince(email, since);
    }

    @Test
    void testGetFailedAttemptsByIp() {
        // Given
        LocalDateTime since = LocalDateTime.now().minusMinutes(15);
        long expectedCount = 5L;
        when(loginAttemptRepository.countFailedAttemptsByIpSince(ipAddress, since)).thenReturn(expectedCount);

        // When
        long result = rateLimitService.getFailedAttemptsByIp(ipAddress, since);

        // Then
        assertEquals(expectedCount, result);
        verify(loginAttemptRepository, times(1)).countFailedAttemptsByIpSince(ipAddress, since);
    }

    @Test
    void testGetTotalAttemptsByEmail() {
        // Given
        LocalDateTime since = LocalDateTime.now().minusMinutes(15);
        long expectedCount = 8L;
        when(loginAttemptRepository.countAttemptsByEmailSince(email, since)).thenReturn(expectedCount);

        // When
        long result = rateLimitService.getTotalAttemptsByEmail(email, since);

        // Then
        assertEquals(expectedCount, result);
        verify(loginAttemptRepository, times(1)).countAttemptsByEmailSince(email, since);
    }

    @Test
    void testGetTotalAttemptsByIp() {
        // Given
        LocalDateTime since = LocalDateTime.now().minusMinutes(15);
        long expectedCount = 12L;
        when(loginAttemptRepository.countAttemptsByIpSince(ipAddress, since)).thenReturn(expectedCount);

        // When
        long result = rateLimitService.getTotalAttemptsByIp(ipAddress, since);

        // Then
        assertEquals(expectedCount, result);
        verify(loginAttemptRepository, times(1)).countAttemptsByIpSince(ipAddress, since);
    }

    @Test
    void testClearRateLimitBuckets() {
        // When
        rateLimitService.clearRateLimitBuckets();

        // Then - Should not throw any exception
        assertDoesNotThrow(() -> rateLimitService.clearRateLimitBuckets());
    }

    @Test
    void testRateLimitWithDifferentEmails() {
        // Given
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";
        when(loginAttemptRepository.save(any(LoginAttempt.class))).thenReturn(new LoginAttempt());

        // When - Exceed limit for email1
        for (int i = 0; i < 6; i++) {
            rateLimitService.isLoginAllowed(email1, ipAddress);
        }

        // Then - email2 should still be allowed
        boolean result = rateLimitService.isLoginAllowed(email2, ipAddress);
        assertTrue(result);
    }

    @Test
    void testRateLimitWithDifferentIps() {
        // Given
        String ip1 = "192.168.1.1";
        String ip2 = "192.168.1.2";
        when(loginAttemptRepository.save(any(LoginAttempt.class))).thenReturn(new LoginAttempt());

        // When - Exceed limit for ip1
        for (int i = 0; i < 11; i++) {
            rateLimitService.isLoginAllowed(email, ip1);
        }

        // Then - ip2 should still be allowed
        boolean result = rateLimitService.isLoginAllowed(email, ip2);
        assertTrue(result);
    }
}
