package com.lms.service;

import com.lms.entity.LoginAttempt;
import com.lms.repository.LoginAttemptRepository;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final LoginAttemptRepository loginAttemptRepository;
    private final AuditService auditService;

    @Value("${lms.rate-limit.login.max-attempts:5}")
    private int maxLoginAttempts;

    @Value("${lms.rate-limit.login.window-minutes:15}")
    private int loginWindowMinutes;

    @Value("${lms.rate-limit.ip.max-attempts:10}")
    private int maxIpAttempts;

    @Value("${lms.rate-limit.ip.window-minutes:15}")
    private int ipWindowMinutes;

    private final ConcurrentHashMap<String, Bucket> emailBuckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    public boolean isLoginAllowed(String email, String ipAddress) {
        // Check email-based rate limiting
        if (!isEmailLoginAllowed(email)) {
            log.warn("Login blocked for email: {} due to rate limiting", email);
            return false;
        }

        // Check IP-based rate limiting
        if (!isIpLoginAllowed(ipAddress)) {
            log.warn("Login blocked for IP: {} due to rate limiting", ipAddress);
            return false;
        }

        return true;
    }

    private boolean isEmailLoginAllowed(String email) {
        Bucket bucket = emailBuckets.computeIfAbsent(email, this::createEmailBucket);
        return bucket.tryConsume(1);
    }

    private boolean isIpLoginAllowed(String ipAddress) {
        Bucket bucket = ipBuckets.computeIfAbsent(ipAddress, this::createIpBucket);
        return bucket.tryConsume(1);
    }

    private Bucket createEmailBucket(String email) {
        Bandwidth limit = Bandwidth.classic(maxLoginAttempts, 
            Refill.intervally(maxLoginAttempts, Duration.ofMinutes(loginWindowMinutes)));
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket createIpBucket(String ipAddress) {
        Bandwidth limit = Bandwidth.classic(maxIpAttempts, 
            Refill.intervally(maxIpAttempts, Duration.ofMinutes(ipWindowMinutes)));
        return Bucket.builder().addLimit(limit).build();
    }

    public void recordLoginAttempt(String email, String ipAddress, String userAgent, boolean success, String failureReason) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setEmail(email);
        attempt.setIpAddress(ipAddress);
        attempt.setSuccess(success);
        attempt.setUserAgent(userAgent);
        attempt.setFailureReason(failureReason);
        attempt.setTimestamp(LocalDateTime.now());

        loginAttemptRepository.save(attempt);

        // Log to audit system
        auditService.logLogin(null, email, ipAddress, userAgent, success, failureReason);
    }

    public long getFailedAttemptsByEmail(String email, LocalDateTime since) {
        return loginAttemptRepository.countFailedAttemptsByEmailSince(email, since);
    }

    public long getFailedAttemptsByIp(String ipAddress, LocalDateTime since) {
        return loginAttemptRepository.countFailedAttemptsByIpSince(ipAddress, since);
    }

    public long getTotalAttemptsByEmail(String email, LocalDateTime since) {
        return loginAttemptRepository.countAttemptsByEmailSince(email, since);
    }

    public long getTotalAttemptsByIp(String ipAddress, LocalDateTime since) {
        return loginAttemptRepository.countAttemptsByIpSince(ipAddress, since);
    }

    public void clearRateLimitBuckets() {
        emailBuckets.clear();
        ipBuckets.clear();
        log.info("Rate limit buckets cleared");
    }
}
