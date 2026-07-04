package com.drms.userservice.service;

import com.drms.userservice.dto.AuthResponse;
import com.drms.userservice.dto.LoginRequest;
import com.drms.userservice.dto.RegisterRequest;
import com.drms.userservice.dto.RegistrationResponse;
import com.drms.userservice.entity.User;
import com.drms.userservice.entity.UserStatus;
import com.drms.userservice.exception.ConflictException;
import com.drms.userservice.exception.UnauthorizedException;
import com.drms.userservice.repository.UserRepository;
import com.drms.userservice.util.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.drms.userservice.client.ShelterServiceClient;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ShelterServiceClient shelterServiceClient;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, ShelterServiceClient shelterServiceClient) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.shelterServiceClient = shelterServiceClient;
    }

    @Transactional
    public RegistrationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email is already registered");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username is already registered");
        }

        UserStatus status = requiresApproval(request) ? UserStatus.PENDING_APPROVAL : UserStatus.ACTIVE;
        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .status(status)
                .build();
        User saved = userRepository.save(user);

        if (saved.getRole() == com.drms.userservice.entity.Role.SHELTER_MANAGER) {
            shelterServiceClient.create(new ShelterServiceClient.ShelterRequest(
                    request.shelterName() != null && !request.shelterName().isBlank() ? request.shelterName() : saved.getFullName() + "'s Shelter",
                    request.shelterDistrict() != null && !request.shelterDistrict().isBlank() ? request.shelterDistrict() : "Default District",
                    request.shelterAddressLine1() != null && !request.shelterAddressLine1().isBlank() ? request.shelterAddressLine1() : "Default Address",
                    request.shelterAddressLine2(),
                    request.shelterContactName() != null && !request.shelterContactName().isBlank() ? request.shelterContactName() : saved.getFullName(),
                    request.shelterContactPhone() != null && !request.shelterContactPhone().isBlank() ? request.shelterContactPhone() : "0000000000",
                    saved.getId(),
                    "INACTIVE"
            ));
        }

        boolean approved = saved.getStatus() == UserStatus.ACTIVE;
        return new RegistrationResponse(
                saved.getId(),
                saved.getFullName(),
                saved.getEmail(),
                saved.getUsername(),
                saved.getRole(),
                saved.getStatus(),
                approved,
                approved ? jwtTokenProvider.createToken(saved) : null,
                approved
                        ? "Account created successfully."
                        : "Account created and submitted for admin approval."
        );
    }

    public AuthResponse login(LoginRequest request) {
        String identifier = request.identifier().trim();
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        if (user.getStatus() == UserStatus.PENDING_APPROVAL) {
            throw new UnauthorizedException("Account is pending admin approval");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("User is not active");
        }
        return new AuthResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getUsername(),
                user.getRole(),
                user.getStatus(),
                jwtTokenProvider.createToken(user)
        );
    }

    private boolean requiresApproval(RegisterRequest request) {
        return true;
    }
}
