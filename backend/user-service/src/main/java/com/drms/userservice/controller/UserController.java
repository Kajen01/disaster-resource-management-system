package com.drms.userservice.controller;

import com.drms.userservice.dto.CreateUserRequest;
import com.drms.userservice.dto.UpdateUserStatusRequest;
import com.drms.userservice.dto.UserResponse;
import com.drms.userservice.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> getAll() {
        return userService.getAll();
    }

    @PostMapping
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable("id") Long id) {
        return userService.getById(id);
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(@RequestHeader("X-User-Email") String email) {
        return userService.getByEmail(email);
    }

    @PatchMapping("/{id}/status")
    public UserResponse updateStatus(@PathVariable("id") Long id, @Valid @RequestBody UpdateUserStatusRequest request) {
        return userService.updateStatus(id, request);
    }

    @GetMapping("/internal/exists/{id}")
    public boolean existsByIdAndRole(
            @PathVariable("id") Long id,
            @RequestParam(value = "role", required = false) String role
    ) {
        return userService.existsByIdAndRole(id, role);
    }
}
