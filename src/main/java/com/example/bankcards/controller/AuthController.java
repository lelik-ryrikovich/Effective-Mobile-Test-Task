package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.JwtAuthenticationResponse;
import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Авторизация пользователей")
public class AuthController {

    private final AuthenticationService authenticationService;

    @Operation(
            summary = "Вход в систему",
            description = "Возвращает JWT токен"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Успешно",
            content = @Content(schema = @Schema(implementation = JwtAuthenticationResponse.class)
    ))
    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }
}

