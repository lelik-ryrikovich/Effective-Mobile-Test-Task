package com.example.bankcards.service;

import com.example.bankcards.dto.JwtAuthenticationResponse;
import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.security.JwtUtil;
import com.example.bankcards.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Сервис аутентификации пользователей.
 *
 * <p>Выполняет проверку логина и пароля и
 * возвращает JWT-токен при успешной аутентификации.</p>
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;

    /**
     * Выполняет вход пользователя в систему.
     *
     * @param request логин и пароль
     * @return JWT-токен
     */
    public JwtAuthenticationResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
        ));

        UserDetails user = userDetailsService.loadUserByUsername(request.getUsername());

        var jwt = jwtUtil.generateToken(user);
        return new JwtAuthenticationResponse(jwt);
    }
}
