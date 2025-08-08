package com.lms.repository;

import com.lms.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    List<UserRole> findByUserIdAndIsActiveTrue(UUID userId);

    List<UserRole> findByRoleIdAndIsActiveTrue(UUID roleId);

    @Query("SELECT ur FROM UserRole ur WHERE ur.user.id = :userId AND ur.role.isActive = true AND ur.isActive = true")
    List<UserRole> findActiveRolesByUserId(@Param("userId") UUID userId);

    boolean existsByUserIdAndRoleIdAndIsActiveTrue(UUID userId, UUID roleId);
}
