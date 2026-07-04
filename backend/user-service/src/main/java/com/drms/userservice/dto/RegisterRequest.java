package com.drms.userservice.dto;

import com.drms.userservice.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String fullName,
        @Email @NotBlank String email,
        @NotBlank String username,
        @NotBlank @Size(min = 8) String password,
        @NotNull Role role,
        
        String shelterName,
        String shelterDistrict,
        String shelterAddressLine1,
        String shelterAddressLine2,
        String shelterContactName,
        String shelterContactPhone,
        Double shelterLatitude,
        Double shelterLongitude
) {
    public RegisterRequest(String fullName, String email, String username, String password, Role role) {
        this(fullName, email, username, password, role, null, null, null, null, null, null, null, null);
    }
}
