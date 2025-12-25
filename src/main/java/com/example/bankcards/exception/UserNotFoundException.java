package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends AppException {
    public UserNotFoundException(Long id) {
        super("Пользователь не найден: " + id, HttpStatus.NOT_FOUND);
    }

    public UserNotFoundException(String username) {
        super("Пользователь не найден: " + username, HttpStatus.NOT_FOUND);
    }
}
