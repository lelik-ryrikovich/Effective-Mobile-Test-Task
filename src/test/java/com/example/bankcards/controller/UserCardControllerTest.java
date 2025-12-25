package com.example.bankcards.controller;

import com.example.bankcards.BaseWebMvcTest;
import com.example.bankcards.dto.CardFilterRequest;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.security.JwtAuthFilter;
import com.example.bankcards.service.UserCardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = UserCardController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthFilter.class
        )
)
class UserCardControllerTest extends BaseWebMvcTest {

    @MockBean
    private UserCardService userCardService;

    // ============================================================
    // GET USER CARDS (masked)
    // ============================================================

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void getUserCards_success() throws Exception {
        Card card = Card.builder()
                .id(1L)
                .maskedNumber("**** **** **** 1234")
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("100.00"))
                .currency("USD")
                .expiry(LocalDate.now().plusYears(2))
                .build();

        Page<Card> page = new PageImpl<>(List.of(card));

        when(userCardService.getUserCards(
                eq("user1"),
                any(CardFilterRequest.class),
                any(PageRequest.class)
        )).thenReturn(page);

        mockMvc.perform(get("/api/user/cards/get"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].maskedNumber").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.content[0].balance").value(100.00));

        verify(userCardService)
                .getUserCards(eq("user1"), any(CardFilterRequest.class), any(PageRequest.class));
    }

    // ============================================================
    // GET DECRYPTED CARDS
    // ============================================================

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void getDecryptedUserCards_success() throws Exception {
        Card card = Card.builder()
                .id(1L)
                .decryptedNumber("1234567812345678")
                .balance(new BigDecimal("50.00"))
                .currency("USD")
                .build();

        Page<Card> page = new PageImpl<>(List.of(card));

        when(userCardService.getDecryptedPanUserCards(
                eq("user1"),
                any(PageRequest.class)
        )).thenReturn(page);

        mockMvc.perform(get("/api/user/cards/get-decrypted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].decryptedNumber")
                        .value("1234567812345678"));

        verify(userCardService)
                .getDecryptedPanUserCards(eq("user1"), any(PageRequest.class));
    }

    // ============================================================
    // GET DECRYPTED CARD BY ID
    // ============================================================

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void getDecryptedUserCard_success() throws Exception {
        Card card = Card.builder()
                .id(1L)
                .decryptedNumber("1234567812345678")
                .balance(new BigDecimal("100.00"))
                .currency("USD")
                .build();

        when(userCardService.getDecryptedPanUserCard("user1", 1L))
                .thenReturn(card);

        mockMvc.perform(get("/api/user/cards/1/get-decrypted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.decryptedNumber")
                        .value("1234567812345678"));

        verify(userCardService)
                .getDecryptedPanUserCard("user1", 1L);
    }

    // ============================================================
    // TRANSFER
    // ============================================================

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void transferBetweenCards_success() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(new BigDecimal("10.00"));

        mockMvc.perform(post("/api/user/cards/transfer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(userCardService)
                .transferBetweenCards("user1", 1L, 2L, new BigDecimal("10.00"));
    }

    // ============================================================
    // GET BALANCE
    // ============================================================

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void getBalance_success() throws Exception {
        when(userCardService.getBalanceByCard(1L, "user1"))
                .thenReturn(new BigDecimal("99.99"));

        mockMvc.perform(get("/api/user/cards/1/balance"))
                .andExpect(status().isOk())
                .andExpect(content().string("99.99"));

        verify(userCardService)
                .getBalanceByCard(1L, "user1");
    }

    // ============================================================
    // REQUEST BLOCK
    // ============================================================

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void requestBlock_success() throws Exception {
        mockMvc.perform(post("/api/user/cards/1/request-block")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userCardService)
                .requestBlockCard(1L, "user1");
    }

    // ============================================================
    // UNAUTHORIZED
    // ============================================================

    @Test
    @WithAnonymousUser
    void getCards_unauthorized() throws Exception {
        mockMvc.perform(get("/api/user/cards/get"))
                .andExpect(status().isUnauthorized());
    }
}