package com.lms.service;

import com.lms.entity.Permission;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.entity.UserRole;
import com.lms.repository.PermissionRepository;
import com.lms.repository.RoleRepository;
import com.lms.repository.UserRepository;
import com.lms.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RbacService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;

    // Role Management
    public List<Role> getAllActiveRoles() {
        return roleRepository.findByIsActiveTrue();
    }

    public Optional<Role> getRoleByName(String name) {
        return roleRepository.findByNameAndIsActiveTrue(name);
    }

    public Role createRole(String name, String description, List<UUID> permissionIds) {
        if (roleRepository.existsByNameAndIsActiveTrue(name)) {
            throw new IllegalArgumentException("Role with name " + name + " already exists");
        }

        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        role.setPermissions(permissions);
        role.setIsActive(true);

        return roleRepository.save(role);
    }

    public Role updateRole(UUID roleId, String name, String description, List<UUID> permissionIds) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        if (!role.getName().equals(name) && roleRepository.existsByNameAndIsActiveTrue(name)) {
            throw new IllegalArgumentException("Role with name " + name + " already exists");
        }

        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        
        role.setName(name);
        role.setDescription(description);
        role.setPermissions(permissions);

        return roleRepository.save(role);
    }

    public void deactivateRole(UUID roleId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        role.setIsActive(false);
        roleRepository.save(role);
    }

    // Permission Management
    public List<Permission> getAllActivePermissions() {
        return permissionRepository.findByIsActiveTrue();
    }

    public List<Permission> getPermissionsByResource(String resource) {
        return permissionRepository.findByResourceAndIsActiveTrue(resource);
    }

    public Permission createPermission(String name, String description, String resource, String action) {
        if (permissionRepository.existsByNameAndIsActiveTrue(name)) {
            throw new IllegalArgumentException("Permission with name " + name + " already exists");
        }

        Permission permission = new Permission();
        permission.setName(name);
        permission.setDescription(description);
        permission.setResource(resource);
        permission.setAction(action);
        permission.setIsActive(true);

        return permissionRepository.save(permission);
    }

    public void deactivatePermission(UUID permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new IllegalArgumentException("Permission not found"));
        permission.setIsActive(false);
        permissionRepository.save(permission);
    }

    // User Role Management
    @Transactional
    public void assignRoleToUser(UUID userId, UUID roleId, UUID assignedBy) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        if (userRoleRepository.existsByUserIdAndRoleIdAndIsActiveTrue(userId, roleId)) {
            throw new IllegalArgumentException("User already has this role");
        }

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setAssignedBy(assignedBy);
        userRole.setIsActive(true);

        userRoleRepository.save(userRole);
        log.info("Role {} assigned to user {}", role.getName(), user.getEmail());
    }

    @Transactional
    public void removeRoleFromUser(UUID userId, UUID roleId) {
        List<UserRole> userRoles = userRoleRepository.findByUserIdAndIsActiveTrue(userId);
        
        userRoles.stream()
            .filter(ur -> ur.getRole().getId().equals(roleId))
            .findFirst()
            .ifPresent(userRole -> {
                userRole.setIsActive(false);
                userRoleRepository.save(userRole);
                log.info("Role {} removed from user {}", userRole.getRole().getName(), userRole.getUser().getEmail());
            });
    }

    public List<UserRole> getUserRoles(UUID userId) {
        return userRoleRepository.findActiveRolesByUserId(userId);
    }

    // Authorization Checks
    public boolean hasPermission(UUID userId, String resource, String action) {
        List<UserRole> userRoles = userRoleRepository.findActiveRolesByUserId(userId);
        
        return userRoles.stream()
            .anyMatch(userRole -> userRole.getRole().getPermissions().stream()
                .anyMatch(permission -> permission.getIsActive() && 
                    permission.getResource().equals(resource) && 
                    permission.getAction().equals(action)));
    }

    public boolean hasRole(UUID userId, String roleName) {
        List<UserRole> userRoles = userRoleRepository.findActiveRolesByUserId(userId);
        return userRoles.stream()
            .anyMatch(userRole -> userRole.getRole().getName().equals(roleName));
    }

    public List<GrantedAuthority> getUserAuthorities(UUID userId) {
        List<UserRole> userRoles = userRoleRepository.findActiveRolesByUserId(userId);
        
        return userRoles.stream()
            .flatMap(userRole -> {
                List<GrantedAuthority> authorities = new java.util.ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + userRole.getRole().getName()));
                
                userRole.getRole().getPermissions().stream()
                    .filter(Permission::getIsActive)
                    .forEach(permission -> 
                        authorities.add(new SimpleGrantedAuthority(
                            permission.getResource() + "_" + permission.getAction()
                        ))
                    );
                
                return authorities.stream();
            })
            .collect(Collectors.toList());
    }
}
