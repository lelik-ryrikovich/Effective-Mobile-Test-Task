package com.example.bankcards.entity;

/**
 * Статусы транзакции.
 */
public enum TransactionStatus {

    /**
     * Транзакция создана и ожидает обработки.
     */
    PENDING,

    /**
     * Транзакция успешно выполнена.
     */
    COMPLETED,

    /**
     * Транзакция завершилась с ошибкой.
     */
    FAILED
}
