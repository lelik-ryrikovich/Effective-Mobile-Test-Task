package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;

public class NegativeTransferAmountException extends AppException {
    public NegativeTransferAmountException() {
        super("Сумма перевода должна быть положительной", HttpStatus.BAD_REQUEST);
    }
}
