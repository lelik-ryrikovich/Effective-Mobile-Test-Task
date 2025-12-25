package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Сущность транзакции между банковскими картами.
 * <p>
 * Отражает перевод средств между картами, включая статус,
 * сумму, валюту и время выполнения операции.
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    /**
     * Уникальный идентификатор транзакции.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Карта-источник средств.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_card_id")
    private Card fromCard;

    /**
     * Карта-получатель средств.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_card_id")
    private Card toCard;

    /**
     * Сумма транзакции.
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /**
     * Валюта транзакции.
     */
    @Column(length = 3)
    private String currency;

    /**
     * Текущий статус транзакции.
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    /**
     * Описание или комментарий к транзакции.
     */
    @Column(length = 255)
    private String description;

    /**
     * Дата и время создания транзакции.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
