package com.lms.service;

import com.lms.dto.auth.LoginRequest;
import com.lms.dto.auth.LoginResponse;
import com.lms.dto.auth.RefreshTokenRequest;
import com.lms.dto.auth.UserInfo;
import com.lms.entity.User;
import com.lms.repository.UserRepository;
import com.lms.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service

public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        try {
            log.debug("Attempting to authenticate user: {}", request.getEmail());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            User user = (User) authentication.getPrincipal();
            log.debug("User authenticated successfully: {}", user.getEmail());

            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            UserInfo userInfo = createUserInfo(user);

            return new LoginResponse(
                    accessToken,
                    refreshToken,
                    jwtService.getExpirationTime(),
                    userInfo
            );

        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", request.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    public LoginResponse refreshToken(RefreshTokenRequest request) {
        try {
            String userEmail = jwtService.extractUsername(request.getRefreshToken());

            User user = userRepository.findByEmailWithDetails(userEmail)
                    .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

            if (jwtService.isTokenValid(request.getRefreshToken(), user)) {
                String accessToken = jwtService.generateToken(user);
                UserInfo userInfo = createUserInfo(user);

                return new LoginResponse(
                        accessToken,
                        request.getRefreshToken(), // Keep the same refresh token
                        jwtService.getExpirationTime(),
                        userInfo
                );
            } else {
                throw new BadCredentialsException("Invalid refresh token");
            }

        } catch (Exception e) {
            log.error("Refresh token validation failed", e);
            throw new BadCredentialsException("Invalid refresh token");
        }
    }

    private UserInfo createUserInfo(User user) {
        String roleName = switch (user.getProfileType().getId()) {
            case 0 -> "SUPER_ADMIN";
            case 1 -> "TEACHER";
            case 2 -> "STUDENT";
            default -> "USER";
        };

        return new UserInfo(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                roleName,
                user.getOrganization() != null ? user.getOrganization().getName() : null,
                user.getClassroom() != null ? user.getClassroom().getName() : null
        );
    }
}