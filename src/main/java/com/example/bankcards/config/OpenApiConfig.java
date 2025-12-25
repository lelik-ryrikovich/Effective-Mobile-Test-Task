package com.example.bankcards.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация OpenAPI (Swagger).
 *
 * <p>Определяет общую информацию о REST API, а также
 * настройки безопасности для авторизации через JWT.</p>
 *
 * <p>Используется для генерации Swagger UI и API-документации.</p>
 */
@Configuration
@AllArgsConstructor
public class OpenApiConfig {

    /**
     * Основная конфигурация OpenAPI.
     *
     * <p>Добавляет:</p>
     * <ul>
     *     <li>метаданные API (название, версия, описание)</li>
     *     <li>схему безопасности Bearer JWT</li>
     *     <li>глобальное требование авторизации</li>
     * </ul>
     *
     * @return объект {@link OpenAPI}
     */
    @Bean
    public OpenAPI defineOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bank REST API")
                        .version("1.0")
                        .description("Это API предоставляет эндпоинты для управления банковскими картами.")
                )
                .components(new Components()
                        .addSecuritySchemes("BearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"));
    }
}
