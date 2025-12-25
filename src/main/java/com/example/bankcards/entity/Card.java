package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Сущность банковской карты.
 * <p>
 * Хранит основные данные карты, включая зашифрованный PAN,
 * владельца, срок действия, статус и баланс.
 * PAN хранится в зашифрованном виде.
 */
@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    /**
     * Уникальный идентификатор карты.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Зашифрованный полный номер карты (PAN).
     */
    @Column(name = "encrypted_number", nullable = false, length = 1024)
    private String encryptedNumber;

    /**
     * Последние 4 цифры номера карты.
     * <p>
     * Используются для отображения пользователю.
     */
    @Column(name = "pan_last4", length = 4)
    private String panLast4;

    /**
     * Владелец карты.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * Дата окончания срока действия карты.
     */
    @Column(name = "expiry", nullable = false)
    private LocalDate expiry;

    /**
     * Текущий статус карты.
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private CardStatus status = CardStatus.ACTIVE;

    /**
     * Баланс карты.
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * Валюта карты.
     */
    @Column(length = 3, nullable = false)
    private String currency = "USD";

    /**
     * Замаскированный номер карты.
     * <p>
     * Не сохраняется в базе данных.
     */
    @Transient
    private String maskedNumber;

    /**
     * Расшифрованный номер карты.
     * <p>
     * Используется только при необходимости и не сохраняется в БД.
     */
    @Transient
    private String decryptedNumber;

    /**
     * AES-ключ, используемый для шифрования номера карты.
     */
    @Column(name = "aes_key", nullable = false)
    private String aesKey;
}