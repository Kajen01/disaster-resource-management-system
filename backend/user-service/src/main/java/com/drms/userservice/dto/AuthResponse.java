package com.drms.userservice.dto;

import com.drms.userservice.entity.Role;
import com.drms.userservice.entity.UserStatus;

public record AuthResponse(
        Long userId,
        String fullName,
        String email,
        String username,
        Role role,
        UserStatus status,
        String token
) {
}
