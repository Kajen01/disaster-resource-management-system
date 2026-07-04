package com.drms.userservice.mapper;

import com.drms.userservice.dto.UserResponse;
import com.drms.userservice.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getUsername(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}
