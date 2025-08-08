package com.lms.service;

import com.lms.dto.brand.BrandRequest;
import com.lms.dto.brand.BrandResponse;
import com.lms.entity.Brand;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.BrandRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class BrandService {

    private final BrandRepository brandRepository;

    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    @Transactional
    public BrandResponse createBrand(BrandRequest request) {
        log.info("Creating new brand with name: {}", request.getName());

        // ayni isimde bir brand var mi
        if (brandRepository.existsByName(request.getName())) {
            throw new IllegalStateException("Brand with name '" + request.getName() + "' already exists");
        }

        Brand brand = new Brand();
        brand.setName(request.getName());
        brand.setCode(generateNextBrandCode());

        Brand savedBrand = brandRepository.save(brand);
        log.info("Brand created successfully: {} with code: {}", savedBrand.getName(), savedBrand.getCode());

        return mapToResponse(savedBrand);
    }

    public List<BrandResponse> getAllBrands() {
        log.info("Fetching all brands");
        List<Brand> brands = brandRepository.findAll();
        return brands.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public BrandResponse getBrandById(UUID id) {
        log.info("Fetching brand with ID: {}", id);
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with ID: " + id));

        return mapToResponse(brand);
    }

    @Transactional
    public BrandResponse updateBrand(UUID id, BrandRequest request) {
        log.info("Updating brand with ID: {} and name: {}", id, request.getName());

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with ID: " + id));

        // ayni isimde baska bir brand var mi
        if (!brand.getName().equals(request.getName()) && brandRepository.existsByName(request.getName())) {
            throw new IllegalStateException("Brand with name '" + request.getName() + "' already exists");
        }

        brand.setName(request.getName());
        Brand savedBrand = brandRepository.save(brand);

        log.info("Brand updated successfully: {}", savedBrand.getName());
        return mapToResponse(savedBrand);
    }

    @Transactional
    public void deleteBrand(UUID id) {
        log.info("Attempting to delete brand with ID: {}", id);

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with ID: " + id));

        // Check if brand has organizations (PDF requirement)
        if (brandRepository.hasOrganizations(id)) {
            throw new IllegalStateException("Cannot delete brand. It has associated organizations. Please remove all organizations first.");
        }

        try {
            brandRepository.delete(brand);
            log.info("Brand deleted successfully: {}", brand.getName());
        } catch (DataIntegrityViolationException e) {
            log.error("Failed to delete brand due to foreign key constraints: {}", e.getMessage());
            throw new IllegalStateException("Cannot delete brand. It has associated records.");
        }
    }

    private String generateNextBrandCode() {
        // Son olusturulan kodu al ve artir.
        String lastCode = brandRepository.findLastGeneratedCode().orElse("krm0");

        int nextNumber;
        if (lastCode.startsWith("krm")) {
            try {
                String numberPart = lastCode.substring(3); // "krm" prefixini kaldir
                nextNumber = Integer.parseInt(numberPart) + 1;
            } catch (NumberFormatException e) {
                // ayristirma basarisiz olursa birden basla
                nextNumber = 1;
            }
        } else {
            nextNumber = 1;
        }

        return "krm" + nextNumber;
    }

    private BrandResponse mapToResponse(Brand brand) {
        int organizationCount = brand.getOrganizations() != null ? brand.getOrganizations().size() : 0;

        return new BrandResponse(
                brand.getId(),
                brand.getName(),
                brand.getCode(),
                organizationCount
        );
    }
}