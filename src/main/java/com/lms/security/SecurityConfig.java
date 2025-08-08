package com.lms.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          UserDetailsService userDetailsService,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    // Public endpoints that don't require authentication
    private static final String[] WHITE_LIST_URL = {
            "/api/v1/auth/**",
            "/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(req -> req
                        .requestMatchers(WHITE_LIST_URL).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // SuperAdmin endpoints - only SUPER_ADMIN can access
                        .requestMatchers(HttpMethod.POST, "/api/v1/brands/**").hasRole("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/brands/**").hasRole("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/brands/**").hasRole("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/brands/**").hasRole("SUPER_ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/v1/organizations/**").hasRole("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/organizations/**").hasRole("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/organizations/**").hasRole("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/organizations/**").hasRole("SUPER_ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/v1/users/**").hasRole("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/**").hasRole("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasRole("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/**").hasRole("SUPER_ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/v1/classrooms/**").hasRole("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/classrooms/**").hasRole("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/classrooms/**").hasRole("SUPER_ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/v1/courses/**").hasRole("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/courses/**").hasRole("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/courses/**").hasRole("SUPER_ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/v1/teachers/assign-classroom").hasRole("SUPER_ADMIN")

                        // Teacher endpoints - only TEACHER can access
                        .requestMatchers("/api/v1/teachers/my-classes").hasRole("TEACHER")
                        .requestMatchers("/api/v1/teachers/my-students").hasRole("TEACHER")
                        .requestMatchers("/api/v1/teachers/my-courses").hasRole("TEACHER")

                        // Student endpoints - only STUDENT can access
                        .requestMatchers("/api/v1/students/my-courses").hasRole("STUDENT")

                        // Any other request must be authenticated
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}