package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;

public class InvalidCardExpiryException extends AppException {
    public InvalidCardExpiryException() {
        super("Срок действия карты должен быть минимум 1 год", HttpStatus.BAD_REQUEST);
    }
}
