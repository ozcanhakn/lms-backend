package com.lms.service;

import com.lms.dto.organization.OrganizationRequest;
import com.lms.dto.organization.OrganizationResponse;
import com.lms.entity.Brand;
import com.lms.entity.Organization;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.BrandRepository;
import com.lms.repository.OrganizationRepository;
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
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final BrandRepository brandRepository;

    public OrganizationService(OrganizationRepository organizationRepository, BrandRepository brandRepository) {
        this.organizationRepository = organizationRepository;
        this.brandRepository = brandRepository;
    }

    @Transactional
    public OrganizationResponse createOrganization(OrganizationRequest request) {
        log.info("Creating new organization with name: {} for brand ID: {}", request.getName(), request.getBrandId());

        // Check if brand exists
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with ID: " + request.getBrandId()));

        // Check if organization with same name and brand already exists
        if (organizationRepository.existsByNameAndBrandId(request.getName(), request.getBrandId())) {
            throw new IllegalStateException("Organization with name '" + request.getName() + "' already exists for this brand");
        }

        Organization organization = new Organization();
        organization.setName(request.getName());
        organization.setBrand(brand);

        Organization savedOrganization = organizationRepository.save(organization);
        log.info("Organization created successfully: {}", savedOrganization.getName());

        return mapToResponse(savedOrganization);
    }

    public List<OrganizationResponse> getAllOrganizations() {
        log.info("Fetching all organizations");
        List<Organization> organizations = organizationRepository.findAll();
        return organizations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OrganizationResponse getOrganizationById(UUID id) {
        log.info("Fetching organization with ID: {}", id);
        Organization organization = organizationRepository.findByIdWithBrand(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + id));

        return mapToResponse(organization);
    }

    @Transactional
    public OrganizationResponse updateOrganization(UUID id, OrganizationRequest request) {
        log.info("Updating organization with ID: {}", id);

        Organization organization = organizationRepository.findByIdWithBrand(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + id));

        // Check if brand exists
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with ID: " + request.getBrandId()));

        // Check if another organization with same name and brand exists
        if (organizationRepository.existsByNameAndBrandId(request.getName(), request.getBrandId()) &&
                (!organization.getName().equals(request.getName()) || !organization.getBrand().getId().equals(request.getBrandId()))) {
            throw new IllegalStateException("Organization with name '" + request.getName() + "' already exists for this brand");
        }

        organization.setName(request.getName());
        organization.setBrand(brand);

        Organization savedOrganization = organizationRepository.save(organization);
        log.info("Organization updated successfully: {}", savedOrganization.getName());

        return mapToResponse(savedOrganization);
    }

    @Transactional
    public void deleteOrganization(UUID id) {
        log.info("Attempting to delete organization with ID: {}", id);

        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + id));

        // Check if organization has classrooms (PDF requirement)
        if (organizationRepository.hasClassrooms(id)) {
            throw new IllegalStateException("Cannot delete organization. It has associated classrooms. Please remove all classrooms first.");
        }

        // Check if organization has users (PDF requirement)
        if (organizationRepository.hasUsers(id)) {
            throw new IllegalStateException("Cannot delete organization. It has associated users. Please remove all users first.");
        }

        try {
            organizationRepository.delete(organization);
            log.info("Organization deleted successfully: {}", organization.getName());
        } catch (DataIntegrityViolationException e) {
            log.error("Failed to delete organization due to foreign key constraints: {}", e.getMessage());
            throw new IllegalStateException("Cannot delete organization. It has associated records.");
        }
    }

    public List<OrganizationResponse> getOrganizationsByBrand(UUID brandId) {
        log.info("Fetching organizations for brand ID: {}", brandId);

        // Verify brand exists
        if (!brandRepository.existsById(brandId)) {
            throw new ResourceNotFoundException("Brand not found with ID: " + brandId);
        }

        List<Organization> organizations = organizationRepository.findByBrandId(brandId);
        return organizations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private OrganizationResponse mapToResponse(Organization organization) {
        int classroomCount = organization.getClassrooms() != null ? organization.getClassrooms().size() : 0;
        int userCount = organization.getUsers() != null ? organization.getUsers().size() : 0;

        return new OrganizationResponse(
                organization.getId(),
                organization.getName(),
                organization.getBrand().getId(),
                organization.getBrand().getName(),
                organization.getBrand().getCode(),
                classroomCount,
                userCount
        );
    }
}