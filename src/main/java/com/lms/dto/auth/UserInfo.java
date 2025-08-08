package com.lms.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String organizationName;
    private String classroomName; // sadece ogrenciler icin
}