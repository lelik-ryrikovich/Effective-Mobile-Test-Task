package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.TransactionStatus;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис управления транзакциями.
 *
 * <p>Отвечает за сохранение транзакций
 * и проверку прав доступа к ним.</p>
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;

    /**
     * Сохраняет транзакцию между картами.
     *
     * @param from карта-отправитель
     * @param to карта-получатель
     * @param amount сумма
     * @param currency валюта
     * @param description описание операции
     */
    public void saveTransaction(Card from, Card to, BigDecimal amount, String currency, String description) {
        Transaction t = Transaction.builder()
                .fromCard(from)
                .toCard(to)
                .amount(amount)
                .currency(currency)
                .description(description)
                .status(TransactionStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(t);
    }

    /**
     * Проверяет, принадлежит ли карта пользователю.
     * @param cardId идентификатор карты
     * @param username имя пользователя
     * @return {@code true}, если карта принадлежит пользователю
     */
    public boolean isCardOwnedByUser(Long cardId, String username) {
        return cardRepository.findById(cardId)
                .map(c -> c.getOwner().getUsername().equals(username))
                .orElse(false);
    }

    /**
     *
     *
     * @throws AccessDeniedException если карта не принадлежит пользователю
     */

    /**
     * Возвращает список всех транзакций (входящие и исходящие) по карте пользователя.
     * @param username имя пользователя
     * @param cardId идентификатор карты
     * @return список транзакций
     * @throws AccessDeniedException если карта не принадлежит пользователю
     */
    public List<Transaction> getUserCardTransactions(String username, Long cardId) {
        if (!isCardOwnedByUser(cardId, username)) {
            throw new AccessDeniedException("Вы не можете смотреть транзакции других пользователей");
        }
        return transactionRepository.findByFromCardIdOrToCardId(cardId, cardId);
    }
}