package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/**
 * Набор спецификаций для фильтрации карт.
 *
 * <p>Используется при поиске карт пользователя
 * с помощью Spring Data JPA Specifications.</p>
 */
public class CardFilter {

    /**
     * Фильтр по статусу карты.
     *
     * @param status статус карты
     */
    public static Specification<Card> hasStatus(CardStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    /**
     * Фильтр по минимальному балансу.
     *
     * @param min минимальный баланс
     */
    public static Specification<Card> balanceGreaterThan(BigDecimal min) {
        return (root, query, cb) ->
                min == null ? null : cb.greaterThanOrEqualTo(root.get("balance"), min);
    }

    /**
     * Фильтр по максимальному балансу.
     *
     * @param max максимальный баланс.
     */
    public static Specification<Card> balanceLessThan(BigDecimal max) {
        return (root, query, cb) ->
                max == null ? null : cb.lessThanOrEqualTo(root.get("balance"), max);
    }

    /**
     * Фильтр по владельцу карты.
     *
     * @param ownerId владелец карты
     */
    public static Specification<Card> ownerId(Long ownerId) {
        return (root, query, cb) ->
                ownerId == null ? null : cb.equal(root.get("owner").get("id"), ownerId);
    }
}