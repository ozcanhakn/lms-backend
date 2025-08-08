package com.lms.aspect;

import com.lms.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;

    @Around("@annotation(com.lms.annotation.Audit)")
    public Object auditMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        // Get request information
        HttpServletRequest request = getCurrentRequest();
        String ipAddress = getClientIpAddress(request);
        String userAgent = request != null ? request.getHeader("User-Agent") : "Unknown";
        
        // Get user information
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = null;
        String userEmail = "anonymous";
        
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            org.springframework.security.core.userdetails.User user = 
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
            userEmail = user.getUsername();
            // You might need to get userId from a custom UserDetails implementation
        }

        String action = determineAction(methodName);
        String resourceType = determineResourceType(className);
        String resourceId = extractResourceId(joinPoint.getArgs());
        String details = String.format("Method: %s.%s", className, methodName);

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            // Log successful operation
            auditService.logActivity(userId, userEmail, action, resourceType, resourceId, 
                                   details + " - Duration: " + duration + "ms", 
                                   ipAddress, userAgent, "SUCCESS", null);
            
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            
            // Log failed operation
            auditService.logActivity(userId, userEmail, action, resourceType, resourceId, 
                                   details + " - Duration: " + duration + "ms", 
                                   ipAddress, userAgent, "ERROR", e.getMessage());
            
            throw e;
        }
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) return "Unknown";
        
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private String determineAction(String methodName) {
        if (methodName.startsWith("create") || methodName.startsWith("save") || methodName.startsWith("add")) {
            return "CREATE";
        } else if (methodName.startsWith("update") || methodName.startsWith("modify")) {
            return "UPDATE";
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            return "DELETE";
        } else if (methodName.startsWith("get") || methodName.startsWith("find") || methodName.startsWith("read")) {
            return "READ";
        } else {
            return "EXECUTE";
        }
    }

    private String determineResourceType(String className) {
        if (className.contains("User")) return "USER";
        if (className.contains("Course")) return "COURSE";
        if (className.contains("Classroom")) return "CLASSROOM";
        if (className.contains("Organization")) return "ORGANIZATION";
        if (className.contains("Role")) return "ROLE";
        if (className.contains("Permission")) return "PERMISSION";
        return "GENERAL";
    }

    private String extractResourceId(Object[] args) {
        if (args == null || args.length == 0) return null;
        
        for (Object arg : args) {
            if (arg instanceof UUID) {
                return arg.toString();
            } else if (arg instanceof String) {
                try {
                    UUID.fromString((String) arg);
                    return (String) arg;
                } catch (IllegalArgumentException e) {
                    // Not a UUID
                }
            }
        }
        return null;
    }
}
