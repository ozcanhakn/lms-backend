package com.lms.config;

import com.lms.entity.Brand;
import com.lms.entity.Organization;
import com.lms.entity.ProfileType;
import com.lms.entity.User;
import com.lms.repository.BrandRepository;
import com.lms.repository.OrganizationRepository;
import com.lms.repository.ProfileTypeRepository;
import com.lms.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final ProfileTypeRepository profileTypeRepository;
    private final UserRepository userRepository;
    private final BrandRepository brandRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(ProfileTypeRepository profileTypeRepository,
                      UserRepository userRepository,
                      BrandRepository brandRepository,
                      OrganizationRepository organizationRepository,
                      PasswordEncoder passwordEncoder) {
        this.profileTypeRepository = profileTypeRepository;
        this.userRepository = userRepository;
        this.brandRepository = brandRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting data initialization...");

        // varsa profil turleri olustur
        createProfileTypes();

        // varsa brand olustur
        Brand defaultBrand = createDefaultBrand();

        // varsa default organization olustur
        Organization defaultOrg = createDefaultOrganization(defaultBrand);

        // varsa default superadmin olustur
        createDefaultSuperAdmin(defaultOrg);

        log.info("Data initialization completed.");
    }

    private void createProfileTypes() {
        // SuperAdmin
        if (!profileTypeRepository.existsById(0)) {
            ProfileType superAdmin = new ProfileType();
            superAdmin.setId(0);
            superAdmin.setName("SuperAdmin");
            profileTypeRepository.save(superAdmin);
            log.info("Created SuperAdmin profile type");
        }

        // Teacher
        if (!profileTypeRepository.existsById(1)) {
            ProfileType teacher = new ProfileType();
            teacher.setId(1);
            teacher.setName("Teacher");
            profileTypeRepository.save(teacher);
            log.info("Created Teacher profile type");
        }

        // Student
        if (!profileTypeRepository.existsById(2)) {
            ProfileType student = new ProfileType();
            student.setId(2);
            student.setName("Student");
            profileTypeRepository.save(student);
            log.info("Created Student profile type");
        }
    }

    private Brand createDefaultBrand() {
        String defaultBrandName = "System Brand";

        // Varolan bir brand var mı kontrol et
        return brandRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> {
                    log.info("Creating default brand...");
                    Brand defaultBrand = new Brand();
                    defaultBrand.setName(defaultBrandName);
                    defaultBrand.setCode("krm1"); // Manuel olarak set ediyoruz

                    Brand saved = brandRepository.save(defaultBrand);
                    log.info("Created default brand: {} with code: {}", defaultBrandName, saved.getCode());
                    return saved;
                });
    }

    private Organization createDefaultOrganization(Brand brand) {
        String defaultOrgName = "System Organization";

        // Varolan bir organizasyon var mı kontrol et
        return organizationRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> {
                    log.info("Creating default organization...");
                    Organization defaultOrg = new Organization();
                    defaultOrg.setName(defaultOrgName);
                    defaultOrg.setBrand(brand); // Brand'i set et

                    Organization saved = organizationRepository.save(defaultOrg);
                    log.info("Created default organization: {}", defaultOrgName);
                    return saved;
                });
    }

    private void createDefaultSuperAdmin(Organization organization) {
        String adminEmail = "admin@lms.com";

        if (!userRepository.existsByEmail(adminEmail)) {
            ProfileType superAdminProfile = profileTypeRepository.findById(0)
                    .orElseThrow(() -> new RuntimeException("SuperAdmin profile type not found"));

            User superAdmin = new User();
            superAdmin.setEmail(adminEmail);
            superAdmin.setPassword(passwordEncoder.encode("123456")); // Default pass
            superAdmin.setFirstName("Super");
            superAdmin.setLastName("Admin");
            superAdmin.setProfileType(superAdminProfile);

            // Organization'ı set et (ÖNEMLİ: Entity relationship kullanıyoruz...!!!)
            superAdmin.setOrganization(organization);

            userRepository.save(superAdmin);
            log.info("Created default SuperAdmin user: {}", adminEmail);
            log.info("Default password: 123456");
        }
    }
}