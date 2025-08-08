package com.lms.repository;

import com.lms.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BrandRepository extends JpaRepository<Brand, UUID> {

    Optional<Brand> findByName(String name);

    Optional<Brand> findByCode(String code);

    boolean existsByName(String name);

    boolean existsByCode(String code);

    // Brand silinmeden önce altında organizasyon olup olmadığını kontrol etmek için
    @Query("SELECT COUNT(o) > 0 FROM Organization o WHERE o.brand.id = :brandId")
    boolean hasOrganizations(@Param("brandId") UUID brandId);

    // Otomatik kod oluşturma için en son kod numarasını bulma
    @Query("SELECT b.code FROM Brand b WHERE b.code LIKE 'krm%' ORDER BY b.code DESC")
    Optional<String> findLastGeneratedCode();
}