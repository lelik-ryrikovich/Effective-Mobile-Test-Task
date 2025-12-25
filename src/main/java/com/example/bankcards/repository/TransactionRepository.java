package com.example.bankcards.repository;

import com.example.bankcards.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Репозиторий для работы с транзакциями.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Возвращает все транзакции, связанные с картой:
     * входящие и исходящие.
     *
     * @param fromCardId ID карты-отправителя
     * @param toCardId ID карты-получателя
     * @return список транзакций
     */
    List<Transaction> findByFromCardIdOrToCardId(Long fromCardId, Long toCardId);
}
