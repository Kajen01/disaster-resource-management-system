package com.drms.userservice.dto;

import com.drms.userservice.entity.Role;
import com.drms.userservice.entity.UserStatus;
import java.time.Instant;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String username,
        Role role,
        UserStatus status,
        Instant createdAt
) {
}
