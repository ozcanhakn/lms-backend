package com.lms.repository;

import com.lms.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    List<Role> findByIsActiveTrue();

    Optional<Role> findByNameAndIsActiveTrue(String name);

    boolean existsByNameAndIsActiveTrue(String name);

    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.id = :permissionId AND r.isActive = true")
    List<Role> findByPermissionId(@Param("permissionId") UUID permissionId);
}
