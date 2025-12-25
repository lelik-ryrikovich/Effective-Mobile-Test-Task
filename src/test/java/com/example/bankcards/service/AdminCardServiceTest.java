package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NoCardBlockRequestException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminCardServiceTest {
    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminCardService adminCardService;

    @Test
    void createCard_success() {
        User user = User.builder()
                .id(1L)
                .username("test")
                .build();

        when(userRepository.findByUsername("test"))
                .thenReturn(Optional.of(user));

        when(cardRepository.save(any(Card.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        int expiresInYears = 3;

        Card card = adminCardService.createCard(
                "test",
                BigDecimal.valueOf(1000),
                expiresInYears
        );

        assertNotNull(card);
        assertEquals(CardStatus.ACTIVE, card.getStatus());
        assertEquals(BigDecimal.valueOf(1000), card.getBalance());
        assertEquals(LocalDate.now().plusYears(expiresInYears), card.getExpiry());
        assertEquals(user, card.getOwner());
        assertNotNull(card.getEncryptedNumber());
        assertNotNull(card.getMaskedNumber());

        verify(userRepository).findByUsername("test");
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void blockCard_success() {
        Card card = Card.builder()
                .id(1L)
                .status(CardStatus.PENDING_BLOCK)
                .build();

        when(cardRepository.findById(1L))
                .thenReturn(Optional.of(card));

        adminCardService.blockCard(1L);

        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void blockCard_wrongStatus_throwsException() {
        Card card = Card.builder()
                .id(1L)
                .status(CardStatus.ACTIVE)
                .build();

        when(cardRepository.findById(1L))
                .thenReturn(Optional.of(card));

        assertThrows(
                NoCardBlockRequestException.class,
                () -> adminCardService.blockCard(1L)
        );

        verify(cardRepository, never()).save(any());
    }

    @Test
    void activateCard_success() {
        Card card = Card.builder()
                .id(1L)
                .status(CardStatus.BLOCKED)
                .build();

        when(cardRepository.findById(1L))
                .thenReturn(Optional.of(card));

        adminCardService.activateCard(1L);

        assertEquals(CardStatus.ACTIVE, card.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void getAllCards_success() {
        Card card1 = Card.builder().panLast4("1234").build();
        Card card2 = Card.builder().panLast4("5678").build();

        when(cardRepository.findAll())
                .thenReturn(List.of(card1, card2));

        List<Card> cards = adminCardService.getAllCards();

        assertEquals(2, cards.size());
        assertNotNull(cards.get(0).getMaskedNumber());
        assertNotNull(cards.get(1).getMaskedNumber());
    }
}