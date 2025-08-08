package com.lms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User implements UserDetails {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profile_id", nullable = false)
    private ProfileType profileType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    // Only for students
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    // For teachers - many-to-many relationship with classrooms
    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TeacherClassroomAssignment> teacherClassroomAssignments;

    // RBAC - many-to-many relationship with roles
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserRole> userRoles;

    // Spring Security UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Legacy role-based authorities for backward compatibility
        String roleName = switch (profileType.getId()) {
            case 0 -> "ROLE_SUPER_ADMIN";
            case 1 -> "ROLE_TEACHER";
            case 2 -> "ROLE_STUDENT";
            default -> "ROLE_USER";
        };
        
        // Add RBAC-based authorities
        List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(roleName));
        
        if (userRoles != null) {
            userRoles.stream()
                .filter(UserRole::getIsActive)
                .forEach(userRole -> {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + userRole.getRole().getName()));
                    // Add permissions as authorities
                    if (userRole.getRole().getPermissions() != null) {
                        userRole.getRole().getPermissions().stream()
                            .filter(Permission::getIsActive)
                            .forEach(permission -> 
                                authorities.add(new SimpleGrantedAuthority(
                                    permission.getResource() + "_" + permission.getAction()
                                ))
                            );
                    }
                });
        }
        
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}