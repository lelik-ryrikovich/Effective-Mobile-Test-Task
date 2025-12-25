package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;

public class AESEncryptionException extends AppException {
    public AESEncryptionException(String message) {
        super("Ошибка AES шифрования: " + message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
