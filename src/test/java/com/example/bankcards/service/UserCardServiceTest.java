package com.example.bankcards.service;

import com.example.bankcards.dto.CardFilterRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.*;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.EncryptionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCardServiceTest {
    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private UserCardService userCardService;

    private User user;
    private Card card1;
    private Card card2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("test");

        card1 = new Card();
        card1.setId(1L);
        card1.setOwner(user);
        card1.setEncryptedNumber("qwh61LPfVAf1D+ERe/VduJYbJ5PbJDUSgT8NILOi/dw=");
        card1.setAesKey("Mv0Wm9L9M7FqQRdV/dF9VTmDwA82rDmJ6dpauXI1Puk=");
        card1.setStatus(CardStatus.ACTIVE);
        card1.setBalance(new BigDecimal("100.00"));

        card2 = new Card();
        card2.setId(2L);
        card2.setOwner(user);
        card2.setEncryptedNumber("Z5bqAhb7OVJUAMyhTJDLeb2v8BhChEy+adJDPMbiN3M=");
        card2.setAesKey("qHn3qAydez1cscTZBLl49ZtX90WMUBClfxOT3m99HGU=");
        card2.setStatus(CardStatus.ACTIVE);
        card2.setBalance(new BigDecimal("50.00"));
    }

    // ============================================================
    // GET USER CARDS
    // ============================================================

    @Test
    void getUserCards_success() {
        when(userRepository.findByUsername("test"))
                .thenReturn(Optional.of(user));

        when(cardRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card1, card2)));

        var result = userCardService.getUserCards("test", null, Pageable.unpaged());

        assertEquals(2, result.getSize());
        verify(userRepository).findByUsername("test");
    }

    @Test
    void getUserCards_userNotFound() {
        when(userRepository.findByUsername("nope"))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                userCardService.getUserCards("nope", null, null)
        );
    }

    @Test
    void getUserCards_withPagination_success() {
        when(userRepository.findByUsername("test"))
                .thenReturn(Optional.of(user));

        Pageable pageable = PageRequest.of(0, 1); // Первая страница, 1 элемент

        when(cardRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(card1), pageable, 2)); // Всего 2 элемента

        var result = userCardService.getUserCards("test", null, pageable);

        assertEquals(1, result.getSize()); // На странице 1 элемент
        assertEquals(2, result.getTotalElements()); // Всего элементов
        assertEquals(2, result.getTotalPages()); // Всего страниц
        assertEquals(1L, result.getContent().get(0).getId());
    }

    // ============================================================
    // GET DECRYPTED PAN
    // ============================================================

    @Test
    void getDecryptedPanUserCard_success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card1));

        try (MockedStatic<EncryptionUtils> mocked = Mockito.mockStatic(EncryptionUtils.class)) {

            mocked.when(() -> EncryptionUtils.decrypt(card1.getEncryptedNumber(), card1.getAesKey()))
                    .thenReturn("4000 3784 9671 4412 9458");

            Card decryptedCard = userCardService.getDecryptedPanUserCard("test", 1L);

            assertEquals("4000 3784 9671 4412 9458", decryptedCard.getDecryptedNumber());
        }
    }

    @Test
    void getDecryptedPanUserCard_cardNotFound() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () ->
                userCardService.getDecryptedPanUserCard("test", 99L)
        );
    }

    @Test
    void getDecryptedPanUserCard_notOwner() {
        User other = new User();
        other.setUsername("other");
        other.setId(999L);
        card1.setOwner(other);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card1));

        assertThrows(AccessDeniedException.class, () ->
                userCardService.getDecryptedPanUserCard("test", 1L)
        );
    }

    // ============================================================
    // TRANSFER
    // ============================================================

    @Test
    void transfer_success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card1));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(card2));

        BigDecimal amount = new BigDecimal("10.00");

        userCardService.transferBetweenCards("test", 1L, 2L, amount);

        assertEquals(new BigDecimal("90.00"), card1.getBalance());
        assertEquals(new BigDecimal("60.00"), card2.getBalance());
        verify(transactionService).saveTransaction(card1, card2, amount, card1.getCurrency(), "Transfer between user cards");
    }

    @Test
    void transfer_invalidAmount() {
        assertThrows(NegativeTransferAmountException.class, () ->
                userCardService.transferBetweenCards("test", 1L, 2L, BigDecimal.ZERO)
        );
    }

    @Test
    void transfer_insufficientFunds() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card1));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(card2));

        assertThrows(InsufficientFundsException.class, () ->
                userCardService.transferBetweenCards("test", 1L, 2L, new BigDecimal("1000"))
        );
    }

    @Test
    void transfer_notOwner() {
        User other = new User();
        other.setUsername("other");
        other.setId(999L);
        card1.setOwner(other);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card1));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(card2));

        assertThrows(AccessDeniedException.class, () ->
                userCardService.transferBetweenCards("test", 1L, 2L, new BigDecimal("10"))
        );
    }

    // ============================================================
    // BALANCE
    // ============================================================

    @Test
    void getBalance_success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card1));
        assertEquals(new BigDecimal("100.00"), userCardService.getBalanceByCard(1L, "test"));
    }

    @Test
    void getBalance_cardNotFound() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () ->
                userCardService.getBalanceByCard(99L, "test")
        );
    }

    @Test
    void getBalance_notOwner() {
        User other = new User();
        other.setUsername("other");
        other.setId(777L);
        card1.setOwner(other);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card1));

        assertThrows(AccessDeniedException.class, () ->
                userCardService.getBalanceByCard(1L,"test")
        );
    }

    // ============================================================
    // BLOCK REQUEST
    // ============================================================

    @Test
    void requestBlock_success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card1));

        userCardService.requestBlockCard(1L, "test");

        assertEquals(CardStatus.PENDING_BLOCK, card1.getStatus());
    }

    @Test
    void requestBlock_wrongStatus() {
        card1.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card1));

        assertThrows(UnsuitableCardStatusForBlockException.class, () ->
                userCardService.requestBlockCard(1L, "test")
        );
    }

    @Test
    void requestBlock_notOwner() {
        User other = new User();
        other.setId(444L);
        other.setUsername("owner");
        card1.setOwner(other);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card1));

        assertThrows(AccessDeniedException.class, () ->
                userCardService.requestBlockCard(1L, "test")
        );
    }

    // ============================================================
    // FILTER TESTS
    // ============================================================

    @Test
    void getUserCards_withStatusFilter_success() {
        when(userRepository.findByUsername("test"))
                .thenReturn(Optional.of(user));

        CardFilterRequest filter = new CardFilterRequest();
        filter.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card1, card2)));

        var result = userCardService.getUserCards("test", filter, Pageable.unpaged());

        assertEquals(2, result.getSize());
        verify(cardRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getUserCards_withMinBalanceFilter_success() {
        when(userRepository.findByUsername("test"))
                .thenReturn(Optional.of(user));

        CardFilterRequest filter = new CardFilterRequest();
        filter.setMinBalance(new BigDecimal("50.00"));

        // Только card1 имеет баланс >= 50
        when(cardRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card1)));

        var result = userCardService.getUserCards("test", filter, Pageable.unpaged());

        assertEquals(1, result.getSize());
        assertEquals(1L, result.getContent().get(0).getId());
    }

    @Test
    void getUserCards_withMaxBalanceFilter_success() {
        when(userRepository.findByUsername("test"))
                .thenReturn(Optional.of(user));

        CardFilterRequest filter = new CardFilterRequest();
        filter.setMaxBalance(new BigDecimal("60.00"));

        // Только card2 имеет баланс <= 60
        when(cardRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card2)));

        var result = userCardService.getUserCards("test", filter, Pageable.unpaged());

        assertEquals(1, result.getSize());
        assertEquals(2L, result.getContent().get(0).getId());
    }

    @Test
    void getUserCards_withMultipleFilters_success() {
        when(userRepository.findByUsername("test"))
                .thenReturn(Optional.of(user));

        CardFilterRequest filter = new CardFilterRequest();
        filter.setStatus(CardStatus.ACTIVE);
        filter.setMinBalance(new BigDecimal("80.00"));
        filter.setMaxBalance(new BigDecimal("120.00"));

        // Только card1 подходит под все критерии
        when(cardRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card1)));

        var result = userCardService.getUserCards("test", filter, Pageable.unpaged());

        assertEquals(1, result.getSize());
        assertEquals(1L, result.getContent().get(0).getId());
        assertEquals(new BigDecimal("100.00"), result.getContent().get(0).getBalance());
    }

    @Test
    void getUserCards_withStatusBlocked_noResults() {
        when(userRepository.findByUsername("test"))
                .thenReturn(Optional.of(user));

        CardFilterRequest filter = new CardFilterRequest();
        filter.setStatus(CardStatus.BLOCKED);

        // Ни одна карта не заблокирована
        when(cardRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        var result = userCardService.getUserCards("test", filter, Pageable.unpaged());

        assertEquals(0, result.getSize());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void getUserCards_filtersCombinedCorrectly() {
        when(userRepository.findByUsername("test"))
                .thenReturn(Optional.of(user));

        CardFilterRequest filter = new CardFilterRequest();
        filter.setStatus(CardStatus.ACTIVE);
        filter.setMinBalance(new BigDecimal("100.00"));
        filter.setMaxBalance(new BigDecimal("100.00"));

        // Только card1 имеет точный баланс 100.00
        when(cardRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card1)));

        var result = userCardService.getUserCards("test", filter, Pageable.unpaged());

        assertEquals(1, result.getSize());
        assertEquals(1L, result.getContent().get(0).getId());
    }
}

