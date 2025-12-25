package com.example.bankcards.controller;

import com.example.bankcards.dto.CreateUserRequest;
import com.example.bankcards.dto.UpdateUserRolesRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Users", description = "Управление пользователями администратором")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(
            summary = "Создать пользователя",
            description = "Администратор создаёт нового пользователя с указанными ролями."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Успешно",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
    )
    @PostMapping("/create")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = adminUserService.createUser(request);
        return ResponseEntity.created(URI.create("/api/admin/users/" + user.getId()))
                .body(toResponse(user));
    }

    @Operation(
            summary = "Получить всех пользователей",
            description = "Возвращает полный список зарегистрированных пользователей."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Успешно",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserResponse.class)))
    )
    @GetMapping("/get-all")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> list = adminUserService.getAllUsers().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @Operation(
            summary = "Обновить роли пользователя",
            description = "Полностью заменяет набор ролей пользователя на указанный в запросе."
    )
    @ApiResponse(responseCode = "204", description = "Успешно")
    @PutMapping("/{userId}/roles/update")
    public ResponseEntity<Void> updateRoles(@PathVariable("userId") Long userId, @RequestBody UpdateUserRolesRequest request) {
        adminUserService.updateRoles(userId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Добавить роль пользователю",
            description = "Добавляет указанную роль пользователю."
    )
    @ApiResponse(responseCode = "204", description = "Успешно")
    @PostMapping("/{userId}/roles/add")
    public ResponseEntity<Void> addRole(@PathVariable("userId") Long userId, @RequestParam("role") String role) {
        adminUserService.addRole(userId, role);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Удалить роль у пользователя",
            description = "Удаляет указанную роль у пользователя."
    )
    @ApiResponse(responseCode = "204", description = "Успешно")
    @PostMapping("/{userId}/roles/remove")
    public ResponseEntity<Void> removeRole(@PathVariable("userId") Long userId, @RequestParam("role") String role) {
        adminUserService.removeRole(userId, role);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Удалить пользователя",
            description = "Удаляет пользователя по его ID."
    )
    @ApiResponse(responseCode = "204", description = "Успешно")
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .build();
    }
}
