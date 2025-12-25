package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;

public class RolesIsEmptyException extends AppException {
    public RolesIsEmptyException() {
        super("Список ролей пуст", HttpStatus.BAD_REQUEST);
    }
}
