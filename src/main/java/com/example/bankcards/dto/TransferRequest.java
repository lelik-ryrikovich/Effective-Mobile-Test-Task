package com.example.bankcards.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {
    @NotNull
    private Long fromCardId;

    @NotNull
    private Long toCardId;

    @NotNull
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    private BigDecimal amount;
}
