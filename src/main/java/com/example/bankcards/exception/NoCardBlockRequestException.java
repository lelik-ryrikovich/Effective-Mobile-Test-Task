package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;

public class NoCardBlockRequestException extends AppException {
    public NoCardBlockRequestException(Long cardId) {
        super("Никто не запрашивал блокировку карты " + cardId, HttpStatus.CONFLICT);
    }
}
