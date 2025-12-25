package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InvalidCardExpiryException;
import com.example.bankcards.exception.NoCardBlockRequestException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.EncryptionUtils;
import com.example.bankcards.util.CardUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Сервис административного управления банковскими картами.
 *
 * <p>Позволяет администратору создавать, активировать, блокировать
 * и удалять карты, а также просматривать все карты в системе.</p>
 */
@Service
@RequiredArgsConstructor
public class AdminCardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    /**
     * Создаёт новую банковскую карту для пользователя.
     *
     * <p>В процессе:
     * <ul>
     *   <li>генерируется номер карты</li>
     *   <li>номер шифруется AES</li>
     *   <li>сохраняются последние 4 цифры PAN</li>
     * </ul>
     *
     * @param username имя пользователя
     * @param balance начальный баланс
     * @param expiresInYears срок действия карты в годах
     * @return созданная карта
     */
    public Card createCard(String username, BigDecimal balance, int expiresInYears) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        if (expiresInYears <= 0) {
            throw new InvalidCardExpiryException();
        }

        // Генерация номера карты
        String rawCardNumber = CardUtils.generateCardNumber();

        // Сохранение последних 4-ех символов номера карты
        String pan_last4 = rawCardNumber.substring(rawCardNumber.length() - 4);

        // Генерация AES-ключа
        String aesKey = EncryptionUtils.generateAesKey();

        String encryptedNumber = EncryptionUtils.encrypt(rawCardNumber, aesKey);

        Card card = new Card();
        card.setEncryptedNumber(encryptedNumber);
        card.setOwner(user);
        card.setExpiry(LocalDate.now().plusYears(expiresInYears));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(balance);
        card.setMaskedNumber(CardUtils.maskCardNumber(pan_last4));
        card.setAesKey(aesKey);
        card.setPanLast4(pan_last4);

        return cardRepository.save(card);
    }

    /**
     * Блокирует карту после запроса пользователя.
     *
     * @param cardId идентификатор карты
     * @throws NoCardBlockRequestException если не было запроса на блокировку
     */
    public void blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        if (!card.getStatus().equals(CardStatus.PENDING_BLOCK)) {
            throw new NoCardBlockRequestException(cardId);
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    /**
     * Активирует карту.
     *
     * @param cardId идентификатор карты
     */
    public void activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);
    }

    /**
     * Удаляет карту из системы.
     *
     * @param cardId идентификатор карты
     */
    public void deleteCard(Long cardId) {
        cardRepository.deleteById(cardId);
    }

    /**
     * Возвращает список всех карт в системе в замаскированном виде.
     *
     * @return список карт
     */
    public List<Card> getAllCards() {
        List<Card> cards = cardRepository.findAll();
        cards.forEach(card ->
                card.setMaskedNumber(CardUtils.maskCardNumber(card.getPanLast4()))
        );
        return cards;
    }
}