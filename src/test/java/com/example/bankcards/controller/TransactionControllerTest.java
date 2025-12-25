package com.example.bankcards.controller;

import com.example.bankcards.BaseWebMvcTest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.TransactionStatus;
import com.example.bankcards.security.JwtAuthFilter;
import com.example.bankcards.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = TransactionController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthFilter.class
        )
)
class TransactionControllerTest extends BaseWebMvcTest {

    @MockBean
    private TransactionService transactionService;

    // ============================================================
    // SUCCESS
    // ============================================================

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void getTransactions_success() throws Exception {
        Card fromCard = Card.builder().id(1L).build();
        Card toCard = Card.builder().id(2L).build();

        Transaction transaction = Transaction.builder()
                .id(10L)
                .fromCard(fromCard)
                .toCard(toCard)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .status(TransactionStatus.COMPLETED)
                .description("Transfer")
                .createdAt(LocalDateTime.now())
                .build();

        when(transactionService.getUserCardTransactions(
                eq("user1"),
                eq(1L)
        )).thenReturn(List.of(transaction));

        mockMvc.perform(get("/api/user/transactions/1/get"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].fromCardId").value(1L))
                .andExpect(jsonPath("$[0].toCardId").value(2L))
                .andExpect(jsonPath("$[0].amount").value(100.00))
                .andExpect(jsonPath("$[0].currency").value("USD"))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$[0].description").value("Transfer"));

        verify(transactionService)
                .getUserCardTransactions("user1", 1L);
    }

    // ============================================================
    // UNAUTHORIZED
    // ============================================================

    @Test
    void getTransactions_unauthorized() throws Exception {
        mockMvc.perform(get("/api/user/transactions/1/get"))
                .andExpect(status().isUnauthorized());
    }
}