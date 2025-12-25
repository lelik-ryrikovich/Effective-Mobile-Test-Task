package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Сущность пользователя системы.
 * <p>
 * Хранит данные для аутентификации и авторизации,
 * включая роли доступа.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * Уникальный идентификатор пользователя.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Логин пользователя.
     */
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    /**
     * Хэш пароля пользователя.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Email пользователя.
     */
    @Column(length = 255)
    private String email;

    /**
     * Дата и время регистрации пользователя.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Роли пользователя.
     * <p>
     * Используются для разграничения прав доступа.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}
