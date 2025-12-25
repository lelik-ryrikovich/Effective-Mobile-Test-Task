package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Репозиторий для управления пользователями системы.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Находит пользователя по имени пользователя.
     *
     * @param username логин пользователя
     * @return пользователь, если найден
     */
    Optional<User> findByUsername(String username);

    /**
     * Проверяет существование пользователя с указанным логином.
     *
     * @param username логин пользователя
     * @return {@code true}, если пользователь существует
     */
    Boolean existsByUsername(String username);
}
