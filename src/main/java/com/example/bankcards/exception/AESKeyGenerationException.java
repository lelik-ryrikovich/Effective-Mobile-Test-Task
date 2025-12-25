package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;

public class AESKeyGenerationException extends AppException {
    public AESKeyGenerationException (String message) {
        super("Ошибка генерации AES ключа: " + message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
