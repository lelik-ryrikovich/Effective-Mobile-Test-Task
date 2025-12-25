package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CardFilterRequest {
    private CardStatus status;
    @PositiveOrZero
    private BigDecimal minBalance;
    @PositiveOrZero
    private BigDecimal maxBalance;
}
