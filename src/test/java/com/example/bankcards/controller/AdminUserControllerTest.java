package com.example.bankcards.controller;

import com.example.bankcards.BaseWebMvcTest;
import com.example.bankcards.dto.CreateUserRequest;
import com.example.bankcards.dto.UpdateUserRolesRequest;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtAuthFilter;
import com.example.bankcards.service.AdminUserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AdminUserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthFilter.class)
)
public class AdminUserControllerTest extends BaseWebMvcTest {

    @MockBean
    private AdminUserService adminUserService;

    // ============================================================
    // CREATE USER
    // ============================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_success() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("john");
        request.setEmail("john@test.com");
        request.setPassword("password");
        request.setRoles(Set.of("USER"));

        Role userRole = Role.builder()
                .id(1L)
                .name("USER")
                .build();

        User user = User.builder()
                .id(1L)
                .username("john")
                .email("john@test.com")
                .roles(Set.of(userRole))
                .createdAt(LocalDateTime.now())
                .build();

        when(adminUserService.createUser(eq(request)))
                .thenReturn(user);

        mockMvc.perform(post("/api/admin/users/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/admin/users/1"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.roles[0]").value("USER"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_validationError() throws Exception {
        CreateUserRequest request = new CreateUserRequest();

        mockMvc.perform(post("/api/admin/users/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithAnonymousUser
    void createUser_forbidden() throws Exception {
        CreateUserRequest request = new CreateUserRequest();

        mockMvc.perform(post("/api/admin/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ============================================================
    // GET ALL USERS
    // ============================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_success() throws Exception {
        Role adminRole = Role.builder()
                .id(1L)
                .name("ADMIN")
                .build();

        User user = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@test.com")
                .roles(Set.of(adminRole))
                .createdAt(LocalDateTime.now())
                .build();

        when(adminUserService.getAllUsers())
                .thenReturn(List.of(user));

        mockMvc.perform(get("/api/admin/users/get-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].username").value("admin"))
                .andExpect(jsonPath("$[0].roles[0]").value("ADMIN"));
    }

    // ============================================================
    // UPDATE ROLES
    // ============================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateRoles_success() throws Exception {
        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setRoles(Set.of("ADMIN", "USER"));

        mockMvc.perform(put("/api/admin/users/1/roles/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(adminUserService).updateRoles(1L, request);
    }

    // ============================================================
    // ADD ROLE
    // ============================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void addRole_success() throws Exception {
        mockMvc.perform(post("/api/admin/users/1/roles/add")
                        .with(csrf())
                        .param("role", "USER"))
                .andExpect(status().isNoContent());

        verify(adminUserService).addRole(1L, "USER");
    }

    // ============================================================
    // REMOVE ROLE
    // ============================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void removeRole_success() throws Exception {
        mockMvc.perform(post("/api/admin/users/1/roles/remove")
                        .with(csrf())
                        .param("role", "USER"))
                .andExpect(status().isNoContent());

        verify(adminUserService).removeRole(1L, "USER");
    }

    // ============================================================
    // DELETE USER
    // ============================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_success() throws Exception {
        mockMvc.perform(delete("/api/admin/users/1/delete")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(adminUserService).deleteUser(1L);
    }

}
