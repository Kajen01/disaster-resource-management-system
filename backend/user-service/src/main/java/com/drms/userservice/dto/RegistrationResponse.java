package com.drms.userservice.dto;

import com.drms.userservice.entity.Role;
import com.drms.userservice.entity.UserStatus;

public record RegistrationResponse(
        Long userId,
        String fullName,
        String email,
        String username,
        Role role,
        UserStatus status,
        boolean approved,
        String token,
        String message
) {
}
