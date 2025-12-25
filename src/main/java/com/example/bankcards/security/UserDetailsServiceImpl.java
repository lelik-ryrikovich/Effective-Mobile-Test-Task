package com.example.bankcards.security;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

/**
 * Сервис загрузки пользователей для Spring Security.
 *
 * <p>Используется при аутентификации для получения данных пользователя
 * из базы данных.</p>
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Загружает пользователя по имени пользователя.
     *
     * @param username логин пользователя
     * @return {@link UserDetails}
     * @throws UsernameNotFoundException если пользователь не найден
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        return new UserDetailsImpl(user);
    }
}