package com.drms.userservice.dto;

import com.drms.userservice.entity.Role;
import com.drms.userservice.entity.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank String fullName,
        @Email @NotBlank String email,
        @NotBlank String username,
        @NotBlank @Size(min = 8) String password,
        @NotNull Role role,
        @NotNull UserStatus status,

        String shelterName,
        String shelterDistrict,
        String shelterAddressLine1,
        String shelterAddressLine2,
        String shelterContactName,
        String shelterContactPhone
) {
    public CreateUserRequest(String fullName, String email, String username, String password, Role role, UserStatus status) {
        this(fullName, email, username, password, role, status, null, null, null, null, null, null);
    }
}
