package com.example.bankcards.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateCardRequest {
    @NotBlank(message = "Имя пользователя не может быть пустым")
    private String username;

    @PositiveOrZero(message = "Баланс не может быть отрицательным")
    private BigDecimal balance;

    @Min(value = 1, message = "Срок действия карты должен быть минимум 1 год")
    private int expiresInYears;
}
