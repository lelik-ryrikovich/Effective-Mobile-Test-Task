package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Сущность роли пользователя.
 * <p>
 * Используется для разграничения доступа и авторизации.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    /**
     * Уникальный идентификатор роли.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название роли.
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;
}
