package com.example.bankcards.service;

import com.example.bankcards.dto.CreateUserRequest;
import com.example.bankcards.dto.UpdateUserRolesRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.Role;
import com.example.bankcards.exception.RoleNotFoundException;
import com.example.bankcards.exception.RolesIsEmptyException;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Сервис административного управления пользователями.
 *
 * <p>Отвечает за создание пользователей, управление ролями
 * и удаление пользователей.</p>
 */
@Service
@RequiredArgsConstructor
public class AdminUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Создаёт нового пользователя.
     *
     * @param request данные пользователя
     * @return созданный пользователь
     */
    @Transactional
    public User createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException();
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setCreatedAt(LocalDateTime.now());
        user.setRoles(resolveRoles(request.getRoles()));
        return userRepository.save(user);
    }

    /**
     * Возвращает всех пользователей системы.
     *
     * @return список пользователей
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Полностью обновляет роли пользователя.
     *
     * @param userId идентификатор пользователя
     * @param request новые роли
     */
    @Transactional
    public void updateRoles(Long userId, UpdateUserRolesRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.setRoles(resolveRoles(request.getRoles()));
        userRepository.save(user);
    }

    /**
     * Добавляет роль пользователю.
     *
     * @param userId идентификатор пользователя
     * @param roleName имя роли
     */
    @Transactional
    public void addRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RoleNotFoundException(roleName));
        user.getRoles().add(role);
        userRepository.save(user);
    }

    /**
     * Удаляет роль у пользователя.
     *
     * @param userId идентификатор пользователя
     * @param roleName имя роли
     */
    @Transactional
    public void removeRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RoleNotFoundException(roleName));
        user.getRoles().remove(role);
        userRepository.save(user);
    }

    /**
     * Удаляет пользователя.
     *
     * @param userId идентификатор пользователя
     */
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        userRepository.deleteById(userId);
    }

    /**
     * Разрешает роли
     * @param roleNames набор ролей
     * @return
     */
    private Set<Role> resolveRoles(Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            throw new RolesIsEmptyException();
        }
        Set<Role> roles = new HashSet<>();
        for (String rn : roleNames) {
            Role r = roleRepository.findByName(rn)
                    .orElseThrow(() -> new RoleNotFoundException(rn));
            roles.add(r);
        }
        return roles;
    }
}