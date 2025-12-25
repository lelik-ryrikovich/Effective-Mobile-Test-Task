package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.AdminCardService;
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
@RequestMapping("api/admin/cards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Cards", description = "Управление банковскими картами администратором")
public class AdminCardController {

    private final AdminCardService adminCardService;

    @Operation(summary = "Создать карту для пользователя")
    @ApiResponse(
            responseCode = "201",
            description = "Успешно",
            content = @Content(schema = @Schema(implementation = CardResponse.class))
    )
    @PostMapping("/create")
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CreateCardRequest request) {
        Card card = adminCardService.createCard(request.getUsername(), request.getBalance(), request.getExpiresInYears());
        CardResponse response = toResponse(card);
        return ResponseEntity.created(URI.create("/api/admin/cards/" + card.getId()))
                .body(response);
    }

    @Operation(
            summary = "Получить все банковские карты в системе",
            description = "Возвращает полный список всех банковских карт в замаскированном виде."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Успешно",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CardResponse.class)))
    )
    @GetMapping("/get-all")
    public ResponseEntity<List<CardResponse>> getAllCards() {
        List<CardResponse> cards = adminCardService.getAllCards().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(cards);
    }

    @Operation(
            summary = "Активировать карту",
            description = "Администратор активирует карту по ID карты."
    )
    @ApiResponse(responseCode = "204", description = "Успешно")
    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateCard(@PathVariable("id") Long id) {
        adminCardService.activateCard(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Заблокировать карту",
            description = "Администратор блокирует карту по ID карты."
    )
    @ApiResponse(responseCode = "204", description = "Успешно")
    @PostMapping("/{cardId}/block")
    public ResponseEntity<Void> blockCard(@PathVariable("cardId") Long cardId) {
        adminCardService.blockCard(cardId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Удалить карту",
            description = "Удаляет карту из системы по ID карты."
    )
    @ApiResponse(responseCode = "204", description = "Успешно")
    @DeleteMapping("/{cardId}/delete")
    public ResponseEntity<Void> deleteCard(@PathVariable("cardId") Long cardId) {
        adminCardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }

    private CardResponse toResponse(Card card) {
        return CardResponse.builder()
                .id(card.getId())
                .ownerId(card.getOwner().getId())
                .maskedNumber(card.getMaskedNumber())
                .status(card.getStatus())
                .balance(card.getBalance())
                .expiry(card.getExpiry())
                .currency(card.getCurrency())
                .build();
    }
}
