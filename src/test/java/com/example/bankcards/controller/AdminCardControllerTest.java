package com.example.bankcards.controller;

import com.example.bankcards.BaseWebMvcTest;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.security.JwtAuthFilter;
import com.example.bankcards.service.AdminCardService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(
        controllers = AdminCardController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthFilter.class
        )
)
class AdminCardControllerTest extends BaseWebMvcTest {

    @MockBean
    private AdminCardService adminCardService;

    // ============================================================
    // CREATE CARD
    // ============================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_success() throws Exception {
        CreateCardRequest request = new CreateCardRequest();
        request.setUsername("test");
        request.setBalance(new BigDecimal("100.00"));
        request.setExpiresInYears(3);

        Card card = Card.builder()
                .id(1L)
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("100.00"))
                .expiry(LocalDate.now().plusYears(3))
                .currency("USD")
                .maskedNumber("**** **** **** 1234")
                .build();

        when(adminCardService.createCard(
                eq("test"),
                eq(new BigDecimal("100.00")),
                eq(3)
        )).thenReturn(card);

        mockMvc.perform(post("/api/admin/cards/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.balance").value(100.00));

        verify(adminCardService)
                .createCard("test", new BigDecimal("100.00"), 3);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_validationError() throws Exception {
        CreateCardRequest request = new CreateCardRequest();
        request.setUsername("");
        request.setBalance(new BigDecimal("100"));
        request.setExpiresInYears(0);

        mockMvc.perform(
                post("/api/admin/cards/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithAnonymousUser
    void createCard_forbidden() throws Exception {
        CreateCardRequest request = new CreateCardRequest();
        request.setUsername("test");
        request.setBalance(new BigDecimal("100"));
        request.setExpiresInYears(2);

        mockMvc.perform(post("/api/admin/cards/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ============================================================
    // GET ALL
    // ============================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCards_success() throws Exception {
        Card card = Card.builder()
                .id(1L)
                .maskedNumber("**** **** **** 1234")
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("50.00"))
                .currency("USD")
                .expiry(LocalDate.now().plusYears(2))
                .build();

        when(adminCardService.getAllCards())
                .thenReturn(List.of(card));

        mockMvc.perform(get("/api/admin/cards/get-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].maskedNumber").value("**** **** **** 1234"));
    }

    // ============================================================
    // ACTIVATE
    // ============================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateCard_success() throws Exception {
        mockMvc.perform(
                post("/api/admin/cards/1/activate")
                        .with(csrf())
                )
                .andExpect(status().isNoContent());

        verify(adminCardService).activateCard(1L);
    }

    // ============================================================
    // BLOCK
    // ============================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void blockCard_success() throws Exception {
        mockMvc.perform(
                post("/api/admin/cards/1/block")
                        .with(csrf())
                )
                .andExpect(status().isNoContent());

        verify(adminCardService).blockCard(1L);
    }

    // ============================================================
    // DELETE
    // ============================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_success() throws Exception {
        mockMvc.perform(
                delete("/api/admin/cards/1/delete")
                        .with(csrf())
                )
                .andExpect(status().isNoContent());

        verify(adminCardService).deleteCard(1L);
    }
}