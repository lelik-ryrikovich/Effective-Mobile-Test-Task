package com.example.bankcards.config;

import com.example.bankcards.security.JwtAuthFilter;
import com.example.bankcards.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Основная конфигурация безопасности приложения.
 *
 * <p>Настраивает:</p>
 * <ul>
 *     <li>JWT-аутентификацию</li>
 *     <li>провайдер аутентификации на основе {@link UserDetailsServiceImpl}</li>
 *     <li>шифрование паролей</li>
 *     <li>доступ к защищённым и публичным эндпоинтам</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Менеджер аутентификации Spring Security.
     *
     * @param config конфигурация аутентификации
     * @return {@link AuthenticationManager}
     * @throws Exception при ошибке инициализации
     */
    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Кодировщик паролей.
     *
     * <p>Используется BCrypt для безопасного хранения паролей.</p>
     *
     * @return {@link PasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Провайдер аутентификации на основе DAO.
     *
     * <p>Использует {@link UserDetailsServiceImpl} и {@link PasswordEncoder}
     * для проверки учётных данных пользователя.</p>
     *
     * @return {@link AuthenticationProvider}
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Основная цепочка фильтров безопасности.
     *
     * <p>Включает:</p>
     * <ul>
     *     <li>отключение CSRF (используется JWT)</li>
     *     <li>публичный доступ к auth и swagger эндпоинтам</li>
     *     <li>JWT-фильтр до {@link UsernamePasswordAuthenticationFilter}</li>
     * </ul>
     *
     * @param http объект {@link HttpSecurity}
     * @return {@link SecurityFilterChain}
     * @throws Exception при ошибке конфигурации
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/swagger-ui/**",
                                "/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(authenticationProvider());

        http.cors();
        return http.build();
    }
}
