package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class CreateUserRequest {
    @NotBlank(message = "Имя пользователя не может быть пустым")
    private String username;
    @NotBlank(message = "Пароль не может быть пустым")
    private String password;
    @NotBlank(message = "Эл. почта не может быть пустой")
    private String email;
    @NotEmpty(message = "Список ролей не может быть пустым")
    private Set<String> roles;
}
