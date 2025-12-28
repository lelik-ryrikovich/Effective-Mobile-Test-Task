package com.example.bankcards.controller;

import com.example.bankcards.dto.CardFilterRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.PagedResponse;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.UserCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;

@RestController
@RequestMapping("/api/user/cards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "User Cards", description = "Работа с картами пользователя")
public class UserCardController {

    private final UserCardService userCardService;

    @GetMapping("/get")
    @Operation(summary = "Получить замаскированные карты пользователя с пагинацией")
    @ApiResponse(
            responseCode = "200",
            description = "Успешно",
            content = @Content(schema = @Schema(implementation = PagedResponse.class))
    )
    public ResponseEntity<PagedResponse<CardResponse>> getUserCards(
            Pageable pageable,
            CardFilterRequest filter,
            Principal principal) {

        Page<Card> cardsPage = userCardService.getUserCards(principal.getName(), filter, pageable);
        Page<CardResponse> responsePage = cardsPage.map(this::toResponse);

        PagedResponse<CardResponse> pagedResponse = PagedResponse.<CardResponse>builder()
                .content(responsePage.getContent())
                .pageNumber(responsePage.getNumber())
                .pageSize(responsePage.getSize())
                .totalElements(responsePage.getTotalElements())
                .totalPages(responsePage.getTotalPages())
                .last(responsePage.isLast())
                .build();

        return ResponseEntity.ok(pagedResponse);
    }

    @Operation(summary = "Получить расшифрованные карты пользователя")
    @ApiResponse(
            responseCode = "200",
            description = "Успешно",
            content = @Content(
                    array = @ArraySchema(
                            schema = @Schema(implementation = PagedResponse.class)
                    )
            )
    )
    @GetMapping("/get-decrypted")
    public ResponseEntity<PagedResponse<CardResponse>> getDecryptedUserCards(Pageable pageable, Principal principal) {
        Page<Card> cardsPage = userCardService.getDecryptedPanUserCards(principal.getName(), pageable);
        Page<CardResponse> responsePage = cardsPage.map(this::toResponse);

        PagedResponse<CardResponse> pagedResponse = PagedResponse.<CardResponse>builder()
                .content(responsePage.getContent())
                .pageNumber(responsePage.getNumber())
                .pageSize(responsePage.getSize())
                .totalElements(responsePage.getTotalElements())
                .totalPages(responsePage.getTotalPages())
                .last(responsePage.isLast())
                .build();

        return ResponseEntity.ok(pagedResponse);
    }

    @Operation(summary = "Получить расшифрованную карту по её ID")
    @ApiResponse(
            responseCode = "200",
            description = "Успешно",
            content = @Content(schema = @Schema(implementation = CardResponse.class))
    )
    @GetMapping("/{cardId}/get-decrypted")
    public ResponseEntity<CardResponse> getDecryptedUserCard(@PathVariable("cardId") Long cardId, Principal principal) {
        Card card = userCardService.getDecryptedPanUserCard(principal.getName(), cardId);
        CardResponse response = toResponse(card);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Перевод между картами пользователя")
    @ApiResponse(responseCode = "204", description = "Успешно")
    @PostMapping("/transfer")
    public ResponseEntity<Void> transferBetweenCards(@Valid @RequestBody TransferRequest request, Principal principal) {
        userCardService.transferBetweenCards(
                principal.getName(),
                request.getFromCardId(),
                request.getToCardId(),
                request.getAmount()
        );
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить баланс карты")
    @ApiResponse(
            responseCode = "200",
            description = "Успешно",
            content = @Content(schema = @Schema(implementation = BigDecimal.class))
    )
    @GetMapping("/{cardId}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable("cardId") Long cardId, Principal principal) {
        BigDecimal balance = userCardService.getBalanceByCard(cardId, principal.getName());
        return ResponseEntity.ok(balance);
    }

    @Operation(summary = "Запросить блокировку карты")
    @ApiResponse(responseCode = "204", description = "Успешно")
    @PostMapping("/{cardId}/request-block")
    public ResponseEntity<Void> requestBlock(@PathVariable("cardId") Long cardId, Principal principal) {
        userCardService.requestBlockCard(cardId, principal.getName());
        return ResponseEntity.noContent().build();
    }

    private CardResponse toResponse(Card card) {
        return CardResponse.builder()
                .id(card.getId())
                .maskedNumber(card.getMaskedNumber())
                .decryptedNumber(card.getDecryptedNumber())
                .status(card.getStatus())
                .balance(card.getBalance())
                .expiry(card.getExpiry())
                .currency(card.getCurrency())
                .build();
    }
}
