package com.drms.userservice.dto;

import com.drms.userservice.entity.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(@NotNull UserStatus status) {
}
