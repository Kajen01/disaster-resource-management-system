package com.drms.userservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.drms.userservice.dto.AuthResponse;
import com.drms.userservice.dto.RegisterRequest;
import com.drms.userservice.dto.RegistrationResponse;
import com.drms.userservice.entity.Role;
import com.drms.userservice.entity.User;
import com.drms.userservice.entity.UserStatus;
import com.drms.userservice.repository.UserRepository;
import com.drms.userservice.service.AuthService;
import com.drms.userservice.dto.LoginRequest;
import com.drms.userservice.exception.UnauthorizedException;
import com.drms.userservice.util.JwtTokenProvider;
import com.drms.userservice.client.ShelterServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private ShelterServiceClient shelterServiceClient;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerCreatesPendingApprovalDonorAccountWithoutToken() {
        RegisterRequest request = new RegisterRequest("Donor User", "donor@test.com", "donor", "Password123", Role.DONOR);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        RegistrationResponse response = authService.register(request);

        assertEquals(1L, response.userId());
        assertEquals(Role.DONOR, response.role());
        assertNull(response.token());
        assertEquals(UserStatus.PENDING_APPROVAL, response.status());
        assertEquals("donor", response.username());
    }

    @Test
    void registerCreatesPendingApprovalManagerAccountWithoutToken() {
        RegisterRequest request = new RegisterRequest("Manager User", "manager@test.com", "manager", "Password123", Role.SHELTER_MANAGER);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });

        RegistrationResponse response = authService.register(request);

        assertEquals(2L, response.userId());
        assertEquals(Role.SHELTER_MANAGER, response.role());
        assertEquals(UserStatus.PENDING_APPROVAL, response.status());
        assertNull(response.token());
        assertEquals("manager", response.username());
    }

    @Test
    void loginSupportsUsernameIdentifier() {
        User user = User.builder()
                .id(3L)
                .fullName("Admin User")
                .email("admin01@gmail.com")
                .username("admin01")
                .passwordHash("encoded")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
        when(userRepository.findByEmail("admin01")).thenReturn(java.util.Optional.empty());
        when(userRepository.findByUsername("admin01")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("Admin@123", "encoded")).thenReturn(true);
        when(jwtTokenProvider.createToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(new LoginRequest("admin01", "Admin@123"));

        assertEquals("admin01", response.username());
        assertEquals("admin01@gmail.com", response.email());
    }

    @Test
    void loginRejectsPendingApprovalAccounts() {
        User user = User.builder()
                .id(4L)
                .fullName("Manager User")
                .email("manager@test.com")
                .username("manager")
                .passwordHash("encoded")
                .role(Role.SHELTER_MANAGER)
                .status(UserStatus.PENDING_APPROVAL)
                .build();
        when(userRepository.findByEmail("manager@test.com")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("Password123", "encoded")).thenReturn(true);

        org.junit.jupiter.api.Assertions.assertThrows(
                UnauthorizedException.class,
                () -> authService.login(new LoginRequest("manager@test.com", "Password123"))
        );
    }
}
