package com.example.bankcards.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {
    private Long id;
    private Long fromCardId;
    private Long toCardId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String description;
    private LocalDateTime createdAt;
}
