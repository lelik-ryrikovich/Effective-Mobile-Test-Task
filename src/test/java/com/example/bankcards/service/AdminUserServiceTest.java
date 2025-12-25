package com.example.bankcards.service;

import com.example.bankcards.dto.CreateUserRequest;
import com.example.bankcards.dto.UpdateUserRolesRequest;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.RoleNotFoundException;
import com.example.bankcards.exception.RolesIsEmptyException;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminUserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminUserService adminUserService;

    @Test
    void createUser_success() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("test");
        request.setPassword("password");
        request.setEmail("test@mail.com");
        request.setRoles(Set.of("USER"));

        Role userRole = Role.builder()
                .id(1L)
                .name("USER")
                .build();

        when(userRepository.existsByUsername("test")).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        User savedUser = adminUserService.createUser(request);

        assertNotNull(savedUser);
        assertEquals("test", savedUser.getUsername());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals("test@mail.com", savedUser.getEmail());
        assertTrue(savedUser.getRoles().contains(userRole));

        verify(userRepository).existsByUsername("test");
        verify(roleRepository).findByName("USER");
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_userAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("test");

        when(userRepository.existsByUsername("test")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> adminUserService.createUser(request));

        verify(userRepository).existsByUsername("test");
        verifyNoMoreInteractions(userRepository, roleRepository, passwordEncoder);
    }

    @Test
    void createUser_rolesEmpty() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("test");
        request.setPassword("password");
        request.setRoles(Set.of());

        when(userRepository.existsByUsername("test")).thenReturn(false);

        assertThrows(RolesIsEmptyException.class,
                () -> adminUserService.createUser(request));
    }

    @Test
    void createUser_roleNotFound() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("test");
        request.setPassword("password");
        request.setRoles(Set.of("ADMIN"));

        when(userRepository.existsByUsername("test")).thenReturn(false);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class,
                () -> adminUserService.createUser(request));
    }

    @Test
    void updateRoles_success() {
        User user = User.builder()
                .id(1L)
                .roles(Set.of())
                .build();

        Role adminRole = Role.builder()
                .id(2L)
                .name("ADMIN")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));

        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setRoles(Set.of("ADMIN"));

        adminUserService.updateRoles(1L, request);

        assertTrue(user.getRoles().contains(adminRole));
        verify(userRepository).save(user);
    }
}