package com.example.bankcards.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Утилитный класс для работы с JWT-токенами.
 *
 * <p>Отвечает за генерацию, парсинг и валидацию JWT.</p>
 */
@Component
public class JwtUtil {

    /** Base64-закодированный секретный ключ. */
    @Value("${jwt.secret}")
    private String secret;

    /** Время жизни токена в миллисекундах. */
    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    /**
     * Получение ключа для подписи токена.
     * @return секретный ключ
     */
    public SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    /**
     * Генерирует JWT для пользователя.
     * @param userDetails данные пользователя
     * @return JWT-токен
     */
    public String generateToken(UserDetails userDetails) {
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Извлекает имя пользователя из JWT.
     * @param token JWT-токен
     * @return имя пользователя
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Извлекает роли пользователя из JWT.
     * @param token JWT-токен
     * @return набор ролей
     */
    public Set<String> extractRoles(String token) {
        return (Set<String>) extractAllClaims(token).get("roles");
    }

    /**
     * Проверяет валидность токена.
     *
     * @param token       JWT-токен
     * @param userDetails данные пользователя
     * @return {@code true}, если токен валиден
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Проверяет, истёк ли срок действия токена.
     *
     * @param token JWT-токен
     * @return {@code true}, если токен просрочен
     */
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    /**
     * Извлекает все claims из JWT.
     *
     * @param token JWT-токен
     * @return данные
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}