package com.drms.userservice.config;

import com.drms.userservice.entity.Role;
import com.drms.userservice.entity.User;
import com.drms.userservice.entity.UserStatus;
import com.drms.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrapInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminFullName;
    private final String adminEmail;
    private final String adminUsername;
    private final String adminPassword;

    public AdminBootstrapInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.admin.full-name:DRMS Administrator}") String adminFullName,
            @Value("${app.bootstrap.admin.email:admin01@gmail.com}") String adminEmail,
            @Value("${app.bootstrap.admin.username:admin01}") String adminUsername,
            @Value("${app.bootstrap.admin.password:Admin@123}") String adminPassword
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminFullName = adminFullName;
        this.adminEmail = adminEmail;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void ensureAdminExists() {
        if (userRepository.existsByEmail(adminEmail) || userRepository.existsByUsername(adminUsername)) {
            return;
        }

        userRepository.save(User.builder()
                .fullName(adminFullName)
                .email(adminEmail)
                .username(adminUsername)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build());
    }
}
