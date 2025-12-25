package com.example.bankcards.repository;

import com.example.bankcards.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Репозиторий для управления ролями пользователей.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Находит роль по её имени.
     *
     * @param name имя роли
     * @return роль, если найдена
     */
    Optional<Role> findByName(String name);
}
