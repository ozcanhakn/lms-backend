package com.lms.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private Integer profileId;
    private String profileName;
    private UUID organizationId;
    private String organizationName;
    private UUID classroomId; // sadece ogrenciler icin
    private String classroomName; // sadece ogrenciler icin
}
