package com.lms.repository;

import com.lms.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    Optional<Organization> findByName(String name);

    List<Organization> findByBrandId(UUID brandId);

    boolean existsByName(String name);

    boolean existsByNameAndBrandId(String name, UUID brandId);

    // Organizasyon silinmeden önce bağlı sınıf olup olmadığını kontrol etmek için
    @Query("SELECT COUNT(c) > 0 FROM Classroom c WHERE c.organization.id = :organizationId")
    boolean hasClassrooms(@Param("organizationId") UUID organizationId);

    // Organizasyon silinmeden önce bağlı kullanıcı olup olmadığını kontrol etmek için
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.organization.id = :organizationId")
    boolean hasUsers(@Param("organizationId") UUID organizationId);

    // Brand ile birlikte fetch etmek için
    @Query("SELECT o FROM Organization o JOIN FETCH o.brand WHERE o.id = :id")
    Optional<Organization> findByIdWithBrand(@Param("id") UUID id);
}