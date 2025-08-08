package com.lms.repository;

import com.lms.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    List<Permission> findByIsActiveTrue();

    List<Permission> findByResourceAndIsActiveTrue(String resource);

    List<Permission> findByActionAndIsActiveTrue(String action);

    @Query("SELECT p FROM Permission p WHERE p.resource = :resource AND p.action = :action AND p.isActive = true")
    List<Permission> findByResourceAndAction(@Param("resource") String resource, @Param("action") String action);

    boolean existsByNameAndIsActiveTrue(String name);
}
