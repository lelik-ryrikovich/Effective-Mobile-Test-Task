package com.example.bankcards.service;

import com.example.bankcards.dto.CardFilterRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.*;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardUtils;
import com.example.bankcards.util.EncryptionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Сервис работы пользователя со своими картами.
 *
 * <p>Позволяет просматривать карты, баланс,
 * выполнять переводы и запрашивать блокировку.</p>
 */
@Service
@RequiredArgsConstructor
public class UserCardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;

    /**
     * Возвращает карты пользователя с фильтрацией и пагинацией.
     * Автоматически маскирует номера карт для безопасности.
     *
     * @param username имя пользователя
     * @param filter критерии фильтрации
     * @param pageable параметры пагинации
     * @return страница с замаскированными картами
     * @throws UserNotFoundException если пользователь не найден
     */
    public Page<Card> getUserCards(String username, CardFilterRequest filter, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        Specification<Card> spec = CardFilter.ownerId(user.getId());

        if (filter != null) {
            if (filter.getStatus() != null)
                spec = spec.and(CardFilter.hasStatus(filter.getStatus()));

            if (filter.getMinBalance() != null)
                spec = spec.and(CardFilter.balanceGreaterThan(filter.getMinBalance()));

            if (filter.getMaxBalance() != null)
                spec = spec.and(CardFilter.balanceLessThan(filter.getMaxBalance()));
        }

        Page<Card> cards = cardRepository.findAll(spec, pageable);

        // Маскируем номера карт
        cards.forEach(card ->
                card.setMaskedNumber(CardUtils.maskCardNumber(card.getPanLast4()))
        );

        return cards;
    }

    /**
     * Возвращает карты пользователя с расшифрованными номерами (PAN).
     *
     * @param username владелец карт
     * @param pageable пагинация
     * @return карты с расшифрованными номерами в поле decryptedNumber
     */
    public Page<Card> getDecryptedPanUserCards(String username, Pageable pageable) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
        Page<Card> cards = cardRepository.findAllByOwner(user, pageable);

        cards.forEach(card ->
                card.setDecryptedNumber(EncryptionUtils.decrypt(card.getEncryptedNumber(), card.getAesKey()))
        );
        return cards;
    }

    /**
     * Возвращает одну карту пользователя с расшифрованным номером.
     *
     * @param username владелец карты
     * @param cardId ID карты
     * @return карта с расшифрованным номером
     */
    public Card getDecryptedPanUserCard(String username, Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        if (!card.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException("Это не ваша карта");
        }
        card.setDecryptedNumber(EncryptionUtils.decrypt(card.getEncryptedNumber(), card.getAesKey()));

        return card;
    }

    /**
     * Перевод денег между своими картами.
     * Проверяет: положительная сумма, существование карт, принадлежность пользователю, достаточность средств.
     * Обновляет балансы и создаёт транзакцию.
     *
     * @param username имя владельца карт
     * @param fromCardId карта-источник
     * @param toCardId карта-получатель
     * @param amount сумма (должна быть > 0)
     */
    public void transferBetweenCards(String username, Long fromCardId, Long toCardId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegativeTransferAmountException();
        }

        Card fromCard = cardRepository.findById(fromCardId)
                .orElseThrow(() -> new CardNotFoundException(fromCardId));
        Card toCard = cardRepository.findById(toCardId)
                .orElseThrow(() -> new CardNotFoundException(toCardId));

        if (!fromCard.getOwner().getUsername().equals(username) ||
                !toCard.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException("Переводы возможны только между своими картами");
        }

        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        transactionService.saveTransaction(
                fromCard,
                toCard,
                amount,
                fromCard.getCurrency(),
                "Transfer between user cards"
        );
    }

    /**
     * Показывает баланс карты.
     * Проверяет, что карта принадлежит пользователю.
     *
     * @param cardId ID карты
     * @param username имя владельца карты
     * @return текущий баланс
     */
    public BigDecimal getBalanceByCard(Long cardId, String username) {
        var card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        if (!card.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException("Нельзя просмотреть баланс чужой карты");
        }
        return card.getBalance();
    }

    /**
     * Запрос на блокировку карты.
     * Можно запросить только для своих активных карт.
     * Переводит карту в статус {@code PENDING_BLOCK}.
     *
     * @param cardId ID карты
     * @param username владелец карты
     */
    public void requestBlockCard(Long cardId, String username) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        if (!card.getOwner().getUsername().equals(username)) {
            throw new AccessDeniedException("Нельзя запросить блокировку чужой карты");
        }

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new UnsuitableCardStatusForBlockException(cardId, card.getStatus().toString());
        }

        card.setStatus(CardStatus.PENDING_BLOCK);
        cardRepository.save(card);
    }
}
