package com.example.bankcards.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Конфигурация CORS (Cross-Origin Resource Sharing).
 *
 * <p>Определяет правила доступа к REST API из внешних источников
 * (например, frontend-приложений, работающих на другом домене).</p>
 *
 * <p>В текущей конфигурации разрешены:</p>
 * <ul>
 *     <li>любые источники</li>
 *     <li>любые HTTP-методы</li>
 *     <li>любые HTTP-заголовки</li>
 * </ul>
 *
 * <p>Используется в связке с Spring Security.</p>
 */
@Configuration
public class CorsConfig {

    /**
     * Источник конфигурации CORS для всего приложения.
     *
     * @return {@link CorsConfigurationSource} с разрешёнными правилами CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
