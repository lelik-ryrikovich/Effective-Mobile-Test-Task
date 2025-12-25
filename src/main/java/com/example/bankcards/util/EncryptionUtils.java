package com.example.bankcards.util;

import com.example.bankcards.exception.AESDecryptionException;
import com.example.bankcards.exception.AESEncryptionException;
import com.example.bankcards.exception.AESKeyGenerationException;
import lombok.experimental.UtilityClass;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Утилиты для AES шифрования.
 */
@UtilityClass
public class EncryptionUtils {
    /**
     * Генерирует AES-256 ключ.
     * @return Base64 ключ
     */
    public String generateAesKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            SecretKey secretKey = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            throw new AESKeyGenerationException(e.getMessage());
        }
    }

    /**
     * Шифрует строку AES-256.
     * @param value строка для шифрования
     * @param base64Key Base64 ключ
     * @return зашифрованная Base64 строка
     */
    public String encrypt(String value, String base64Key) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(Base64.getDecoder().decode(base64Key), "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new AESEncryptionException(e.getMessage());
        }
    }

    /**
     * Расшифровывает AES-256 строку.
     * @param encryptedValue зашифрованная Base64 строка
     * @param base64AESKey Base64 ключ
     * @return расшифрованная строка
     */
    public String decrypt(String encryptedValue, String base64AESKey) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(Base64.getDecoder().decode(base64AESKey), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedValue)));
        } catch (Exception e) {
            throw new AESDecryptionException(e.getMessage());
        }
    }
}
