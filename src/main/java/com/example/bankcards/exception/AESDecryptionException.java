package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;

public class AESDecryptionException extends AppException {
    public AESDecryptionException(String message) {

        super("Ошибка AES дешифрования: " + message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
