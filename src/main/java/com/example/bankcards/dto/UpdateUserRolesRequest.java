package com.example.bankcards.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateUserRolesRequest {
    @NotEmpty(message = "Список ролей не может быть пустым")
    private Set<String> roles;
}
