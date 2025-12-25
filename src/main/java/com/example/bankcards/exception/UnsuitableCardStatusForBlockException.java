package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;

public class UnsuitableCardStatusForBlockException extends AppException {
    public UnsuitableCardStatusForBlockException(Long cardId, String cardStatus) {
        super("Карту " + cardId + " нельзя заблокировать в текущем статусе: " + cardStatus,
                HttpStatus.CONFLICT);
    }
}
