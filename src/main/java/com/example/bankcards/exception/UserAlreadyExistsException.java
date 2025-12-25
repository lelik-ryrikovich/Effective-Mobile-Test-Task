package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends AppException{
    public UserAlreadyExistsException() {
        super("Такой пользователь уже существует", HttpStatus.CONFLICT);
    }
}
