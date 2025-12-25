package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Глобальный обработчик исключений контроллеров.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Обработка ошибок валидации
     * @param ex исключение MethodArgumentNotValid
     * @return http ответ (400 Bad Request)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Обработка пользовательских исключений
     * @param ex пользовательское исключение
     * @return http ответ
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<?> handleAppException(AppException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(Map.of("code", ex.getStatus(), "error", ex.getMessage()));
    }

    /**
     * Обработка ошибок доступа
     * @param ex исключение AccessDenied
     * @return http ответ (403 Forbidden)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(Exception ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }

    /**
     * Обработка ошибок неверных учетных данных
     * @param ex исключение BadCredentials
     * @return http ответ (401 Unauthorized)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(Exception ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Обработка непредвиденных ошибок
     * @param ex непредвиденная ошибка
     * @return http ответ
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", ex.getMessage()));
    }
}