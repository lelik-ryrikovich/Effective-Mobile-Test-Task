package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;

public class CardNotFoundException extends AppException {
    public CardNotFoundException(Long id) {
        super("Карта не найдена: " + id, HttpStatus.NOT_FOUND);
    }
}
