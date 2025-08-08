package com.lms.service;

import com.lms.entity.Permission;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.entity.UserRole;
import com.lms.repository.PermissionRepository;
import com.lms.repository.RoleRepository;
import com.lms.repository.UserRepository;
import com.lms.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RbacServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RbacService rbacService;

    private UUID userId;
    private UUID roleId;
    private UUID permissionId;
    private User user;
    private Role role;
    private Permission permission;
    private UserRole userRole;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        roleId = UUID.randomUUID();
        permissionId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");

        permission = new Permission();
        permission.setId(permissionId);
        permission.setName("user_read");
        permission.setResource("USER");
        permission.setAction("READ");
        permission.setIsActive(true);

        role = new Role();
        role.setId(roleId);
        role.setName("Test Role");
        role.setDescription("Test Description");
        role.setPermissions(List.of(permission));
        role.setIsActive(true);

        userRole = new UserRole();
        userRole.setId(UUID.randomUUID());
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setIsActive(true);
    }

    @Test
    void testGetAllActiveRoles() {
        // Given
        List<Role> expectedRoles = List.of(role);
        when(roleRepository.findByIsActiveTrue()).thenReturn(expectedRoles);

        // When
        List<Role> result = rbacService.getAllActiveRoles();

        // Then
        assertEquals(expectedRoles, result);
        verify(roleRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void testGetRoleByName() {
        // Given
        String roleName = "Test Role";
        when(roleRepository.findByNameAndIsActiveTrue(roleName)).thenReturn(Optional.of(role));

        // When
        Optional<Role> result = rbacService.getRoleByName(roleName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(role, result.get());
        verify(roleRepository, times(1)).findByNameAndIsActiveTrue(roleName);
    }

    @Test
    void testCreateRole() {
        // Given
        String name = "New Role";
        String description = "New Description";
        List<UUID> permissionIds = List.of(permissionId);
        List<Permission> permissions = List.of(permission);

        when(roleRepository.existsByNameAndIsActiveTrue(name)).thenReturn(false);
        when(permissionRepository.findAllById(permissionIds)).thenReturn(permissions);
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        // When
        Role result = rbacService.createRole(name, description, permissionIds);

        // Then
        assertNotNull(result);
        verify(roleRepository, times(1)).existsByNameAndIsActiveTrue(name);
        verify(permissionRepository, times(1)).findAllById(permissionIds);
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    void testCreateRole_AlreadyExists() {
        // Given
        String name = "Existing Role";
        String description = "Description";
        List<UUID> permissionIds = List.of(permissionId);

        when(roleRepository.existsByNameAndIsActiveTrue(name)).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            rbacService.createRole(name, description, permissionIds));
        verify(roleRepository, times(1)).existsByNameAndIsActiveTrue(name);
        verify(permissionRepository, never()).findAllById(any());
        verify(roleRepository, never()).save(any());
    }

    @Test
    void testUpdateRole() {
        // Given
        String name = "Updated Role";
        String description = "Updated Description";
        List<UUID> permissionIds = List.of(permissionId);
        List<Permission> permissions = List.of(permission);

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleRepository.existsByNameAndIsActiveTrue(name)).thenReturn(false);
        when(permissionRepository.findAllById(permissionIds)).thenReturn(permissions);
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        // When
        Role result = rbacService.updateRole(roleId, name, description, permissionIds);

        // Then
        assertNotNull(result);
        verify(roleRepository, times(1)).findById(roleId);
        verify(permissionRepository, times(1)).findAllById(permissionIds);
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    void testUpdateRole_RoleNotFound() {
        // Given
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            rbacService.updateRole(roleId, "name", "description", List.of()));
        verify(roleRepository, times(1)).findById(roleId);
        verify(roleRepository, never()).save(any());
    }

    @Test
    void testDeactivateRole() {
        // Given
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        // When
        rbacService.deactivateRole(roleId);

        // Then
        verify(roleRepository, times(1)).findById(roleId);
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    void testGetAllActivePermissions() {
        // Given
        List<Permission> expectedPermissions = List.of(permission);
        when(permissionRepository.findByIsActiveTrue()).thenReturn(expectedPermissions);

        // When
        List<Permission> result = rbacService.getAllActivePermissions();

        // Then
        assertEquals(expectedPermissions, result);
        verify(permissionRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void testGetPermissionsByResource() {
        // Given
        String resource = "USER";
        List<Permission> expectedPermissions = List.of(permission);
        when(permissionRepository.findByResourceAndIsActiveTrue(resource)).thenReturn(expectedPermissions);

        // When
        List<Permission> result = rbacService.getPermissionsByResource(resource);

        // Then
        assertEquals(expectedPermissions, result);
        verify(permissionRepository, times(1)).findByResourceAndIsActiveTrue(resource);
    }

    @Test
    void testCreatePermission() {
        // Given
        String name = "new_permission";
        String description = "New Permission";
        String resource = "COURSE";
        String action = "CREATE";

        when(permissionRepository.existsByNameAndIsActiveTrue(name)).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenReturn(permission);

        // When
        Permission result = rbacService.createPermission(name, description, resource, action);

        // Then
        assertNotNull(result);
        verify(permissionRepository, times(1)).existsByNameAndIsActiveTrue(name);
        verify(permissionRepository, times(1)).save(any(Permission.class));
    }

    @Test
    void testCreatePermission_AlreadyExists() {
        // Given
        String name = "existing_permission";
        when(permissionRepository.existsByNameAndIsActiveTrue(name)).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            rbacService.createPermission(name, "description", "resource", "action"));
        verify(permissionRepository, times(1)).existsByNameAndIsActiveTrue(name);
        verify(permissionRepository, never()).save(any());
    }

    @Test
    void testAssignRoleToUser() {
        // Given
        UUID assignedBy = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(userRoleRepository.existsByUserIdAndRoleIdAndIsActiveTrue(userId, roleId)).thenReturn(false);
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(userRole);

        // When
        rbacService.assignRoleToUser(userId, roleId, assignedBy);

        // Then
        verify(userRepository, times(1)).findById(userId);
        verify(roleRepository, times(1)).findById(roleId);
        verify(userRoleRepository, times(1)).existsByUserIdAndRoleIdAndIsActiveTrue(userId, roleId);
        verify(userRoleRepository, times(1)).save(any(UserRole.class));
    }

    @Test
    void testAssignRoleToUser_UserNotFound() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            rbacService.assignRoleToUser(userId, roleId, UUID.randomUUID()));
        verify(userRepository, times(1)).findById(userId);
        verify(roleRepository, never()).findById(any());
        verify(userRoleRepository, never()).save(any());
    }

    @Test
    void testAssignRoleToUser_RoleNotFound() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            rbacService.assignRoleToUser(userId, roleId, UUID.randomUUID()));
        verify(userRepository, times(1)).findById(userId);
        verify(roleRepository, times(1)).findById(roleId);
        verify(userRoleRepository, never()).save(any());
    }

    @Test
    void testAssignRoleToUser_AlreadyAssigned() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(userRoleRepository.existsByUserIdAndRoleIdAndIsActiveTrue(userId, roleId)).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            rbacService.assignRoleToUser(userId, roleId, UUID.randomUUID()));
        verify(userRoleRepository, times(1)).existsByUserIdAndRoleIdAndIsActiveTrue(userId, roleId);
        verify(userRoleRepository, never()).save(any());
    }

    @Test
    void testRemoveRoleFromUser() {
        // Given
        List<UserRole> userRoles = List.of(userRole);
        when(userRoleRepository.findByUserIdAndIsActiveTrue(userId)).thenReturn(userRoles);
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(userRole);

        // When
        rbacService.removeRoleFromUser(userId, roleId);

        // Then
        verify(userRoleRepository, times(1)).findByUserIdAndIsActiveTrue(userId);
        verify(userRoleRepository, times(1)).save(any(UserRole.class));
    }

    @Test
    void testGetUserRoles() {
        // Given
        List<UserRole> expectedUserRoles = List.of(userRole);
        when(userRoleRepository.findActiveRolesByUserId(userId)).thenReturn(expectedUserRoles);

        // When
        List<UserRole> result = rbacService.getUserRoles(userId);

        // Then
        assertEquals(expectedUserRoles, result);
        verify(userRoleRepository, times(1)).findActiveRolesByUserId(userId);
    }

    @Test
    void testHasPermission() {
        // Given
        List<UserRole> userRoles = List.of(userRole);
        when(userRoleRepository.findActiveRolesByUserId(userId)).thenReturn(userRoles);

        // When
        boolean result = rbacService.hasPermission(userId, "USER", "READ");

        // Then
        assertTrue(result);
        verify(userRoleRepository, times(1)).findActiveRolesByUserId(userId);
    }

    @Test
    void testHasPermission_NoPermission() {
        // Given
        List<UserRole> userRoles = List.of(userRole);
        when(userRoleRepository.findActiveRolesByUserId(userId)).thenReturn(userRoles);

        // When
        boolean result = rbacService.hasPermission(userId, "USER", "DELETE");

        // Then
        assertFalse(result);
        verify(userRoleRepository, times(1)).findActiveRolesByUserId(userId);
    }

    @Test
    void testHasRole() {
        // Given
        List<UserRole> userRoles = List.of(userRole);
        when(userRoleRepository.findActiveRolesByUserId(userId)).thenReturn(userRoles);

        // When
        boolean result = rbacService.hasRole(userId, "Test Role");

        // Then
        assertTrue(result);
        verify(userRoleRepository, times(1)).findActiveRolesByUserId(userId);
    }

    @Test
    void testHasRole_NoRole() {
        // Given
        List<UserRole> userRoles = List.of(userRole);
        when(userRoleRepository.findActiveRolesByUserId(userId)).thenReturn(userRoles);

        // When
        boolean result = rbacService.hasRole(userId, "Non-existent Role");

        // Then
        assertFalse(result);
        verify(userRoleRepository, times(1)).findActiveRolesByUserId(userId);
    }

    @Test
    void testGetUserAuthorities() {
        // Given
        List<UserRole> userRoles = List.of(userRole);
        when(userRoleRepository.findActiveRolesByUserId(userId)).thenReturn(userRoles);

        // When
        List<GrantedAuthority> result = rbacService.getUserAuthorities(userId);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_Test Role")));
        assertTrue(result.stream().anyMatch(auth -> auth.getAuthority().equals("USER_READ")));
        verify(userRoleRepository, times(1)).findActiveRolesByUserId(userId);
    }
}
