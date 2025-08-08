package com.lms.dto.user;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotNull(message = "Profile ID is required")
    @Min(value = 1, message = "Profile ID must be 1 (Teacher) or 2 (Student)")
    @Max(value = 2, message = "Profile ID must be 1 (Teacher) or 2 (Student)")
    private Integer profileId; // 1: Ogretmen, 2: Ogrenci (SuperAdmin API araciligiyla olusturulamaz)

    @NotNull(message = "Organization ID is required")
    private UUID organizationId;

    // Sadece ogrenciler (profile_id = 2)
    private UUID classroomId;
}
