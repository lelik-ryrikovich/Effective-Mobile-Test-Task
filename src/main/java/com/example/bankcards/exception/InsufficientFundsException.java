package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;

public class InsufficientFundsException extends AppException {
    public InsufficientFundsException() {
        super("Недостаточно средств", HttpStatus.PAYMENT_REQUIRED);
    }
}
