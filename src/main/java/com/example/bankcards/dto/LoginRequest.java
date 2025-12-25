package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Имя пользователя не может быть пустым")
    String username;
    @NotBlank(message = "Пароль не может быть пустым")
    String password;
}
