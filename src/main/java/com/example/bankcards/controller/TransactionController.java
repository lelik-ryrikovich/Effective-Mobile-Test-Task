package com.example.bankcards.controller;

import com.example.bankcards.dto.TransactionResponse;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/user/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Операции по транзакциям пользователя")
@PreAuthorize("hasRole('USER')")
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(
            summary = "Получить транзакции по карте",
            description = "Возвращает как входящие, так и исходящие транзакции",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешно"),
            }
    )
    @GetMapping("/{cardId}/get")
    public ResponseEntity<List<TransactionResponse>> getTransactions(@PathVariable("cardId") Long cardId, Principal principal) {
        List<TransactionResponse> transactions = transactionService.getUserCardTransactions(principal.getName(), cardId)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(transactions);
    }

    public TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .fromCardId(t.getFromCard() != null ? t.getFromCard().getId() : null)
                .toCardId(t.getToCard() != null ? t.getToCard().getId() : null)
                .amount(t.getAmount())
                .currency(t.getCurrency())
                .status(t.getStatus().name())
                .description(t.getDescription())
                .createdAt(t.getCreatedAt())
                .build();
    }
}

