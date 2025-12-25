package com.example.bankcards.security;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Реализация {@link UserDetails} на основе сущности {@link User}.
 *
 * <p>Используется Spring Security для аутентификации и авторизации.</p>
 */
public class UserDetailsImpl implements UserDetails {

    private final User user;

    public UserDetailsImpl(User user) {
        this.user = user;
    }

    /**
     * Возвращает роли пользователя в формате Spring Security.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(Role::getName)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    @Override public String getPassword() { return user.getPassword(); }
    @Override public String getUsername() { return user.getUsername(); }
    @Override public boolean isEnabled() { return true; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

}

