package com.example.bankcards.security;

import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT-фильтр аутентификации.
 *
 * <p>Перехватывает каждый HTTP-запрос, извлекает JWT-токен из заголовка
 * {@code Authorization}, валидирует его и устанавливает аутентификацию
 * в {@link SecurityContextHolder}.</p>
 *
 * <p>Публичные эндпоинты (auth, swagger, api-docs) пропускаются без проверки.</p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    /** Префикс JWT-токена в заголовке Authorization */
    public static final String BEARER_PREFIX = "Bearer ";

    /** Имя HTTP-заголовка, содержащего токен */
    public static final String HEADER_NAME = "Authorization";

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // Публичные эндпоинты
        if (path.startsWith("/swagger-ui")
                || path.startsWith("/api-docs")
                || path.startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(HEADER_NAME);

        // Отсутствует или некорректный заголовок Authorization
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        String username;

        try {
            username = jwtUtil.extractUsername(token);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (StringUtils.isNotEmpty(username)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (!jwtUtil.isTokenValid(token, userDetails)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}
