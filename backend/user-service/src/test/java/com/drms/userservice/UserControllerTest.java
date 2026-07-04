package com.drms.userservice;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.drms.userservice.controller.UserController;
import com.drms.userservice.dto.UserResponse;
import com.drms.userservice.entity.Role;
import com.drms.userservice.entity.UserStatus;
import com.drms.userservice.service.UserService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = UserController.class, properties = {
        "spring.cloud.config.enabled=false",
        "spring.config.import="
})
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void getAllReturnsUserList() throws Exception {
        when(userService.getAll()).thenReturn(List.of(
                new UserResponse(1L, "Admin User", "admin@example.com", "admin", Role.ADMIN, UserStatus.ACTIVE, Instant.parse("2026-05-17T10:15:30Z"))
        ));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("admin@example.com"))
                .andExpect(jsonPath("$[0].role").value("ADMIN"));

        verify(userService).getAll();
    }

    @Test
    void getCurrentUserUsesGatewayEmailHeader() throws Exception {
        when(userService.getByEmail("manager@example.com"))
                .thenReturn(new UserResponse(
                        2L,
                        "Shelter Manager",
                        "manager@example.com",
                        "manager1",
                        Role.SHELTER_MANAGER,
                        UserStatus.ACTIVE,
                        Instant.parse("2026-05-17T10:15:30Z")
                ));

        mockMvc.perform(get("/api/users/me")
                        .header("X-User-Email", "manager@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("manager1"))
                .andExpect(jsonPath("$.role").value("SHELTER_MANAGER"));

        verify(userService).getByEmail("manager@example.com");
    }

    @Test
    void createUserReturnsCreatedUser() throws Exception {
        when(userService.createUser(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new UserResponse(
                        3L,
                        "Shelter Manager",
                        "manager2@example.com",
                        "manager2",
                        Role.SHELTER_MANAGER,
                        UserStatus.ACTIVE,
                        Instant.parse("2026-05-17T10:15:30Z")
                ));

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content("""
                                {
                                  "fullName": "Shelter Manager",
                                  "email": "manager2@example.com",
                                  "username": "manager2",
                                  "password": "Password123",
                                  "role": "SHELTER_MANAGER",
                                  "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("manager2@example.com"))
                .andExpect(jsonPath("$.role").value("SHELTER_MANAGER"));
    }

    @Test
    void existsByIdAndRoleReturnsBooleanForInternalValidation() throws Exception {
        when(userService.existsByIdAndRole(5L, "SHELTER_MANAGER")).thenReturn(true);

        mockMvc.perform(get("/api/users/internal/exists/5")
                        .param("role", "SHELTER_MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(userService).existsByIdAndRole(5L, "SHELTER_MANAGER");
    }
}
