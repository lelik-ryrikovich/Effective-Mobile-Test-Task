package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;

public class RoleNotFoundException extends AppException {
    public RoleNotFoundException(String roleName) {
        super("Не найдена роль: " + roleName, HttpStatus.NOT_FOUND);
    }
}
