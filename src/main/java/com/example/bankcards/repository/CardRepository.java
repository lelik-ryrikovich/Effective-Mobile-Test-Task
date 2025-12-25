package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Репозиторий для работы с банковскими картами.
 */
public interface CardRepository extends JpaRepository<Card, Long>,
        JpaSpecificationExecutor<Card> {

    /**
     * Возвращает карты, принадлежащие указанному пользователю.
     *
     * @param owner владелец карт
     * @param pageable параметры пагинации
     * @return страница карт пользователя
     */
    Page<Card> findAllByOwner(User owner, Pageable pageable);
}
