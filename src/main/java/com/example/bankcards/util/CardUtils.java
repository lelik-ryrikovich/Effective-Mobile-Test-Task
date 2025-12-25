package com.example.bankcards.util;

import lombok.experimental.UtilityClass;

/**
 * Утилиты для работы с картами.
 */
@UtilityClass
public class CardUtils {

    /**
     * Генерирует номер карты.
     * @return номер карты вида "4000 1234 5678 9012"
     */
    public String generateCardNumber() {
        StringBuilder sb = new StringBuilder("4000 "); // Префикс банка
        for (int i = 0; i < 3; i++) {
            sb.append(String.format("%04d ", (int) (Math.random() * 10000)));
        }
        sb.append(String.format("%04d", (int) (Math.random() * 10000)));
        return sb.toString().trim();
    }

    /**
     * Маскирует номер карты.
     * @param pan_last4 последние 4 цифры
     * @return замаскированный номер вида "**** **** **** 1234"
     */
    public String maskCardNumber(String pan_last4) {
        return "**** **** **** " + pan_last4;
    }
}
