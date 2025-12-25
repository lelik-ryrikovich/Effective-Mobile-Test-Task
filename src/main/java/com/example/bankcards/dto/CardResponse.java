package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class CardResponse {
    private Long id;
    private Long ownerId;
    private String maskedNumber;
    private String decryptedNumber;
    private CardStatus status;
    private BigDecimal balance;
    private LocalDate expiry;
    private String currency;
}