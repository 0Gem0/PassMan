package com.Passman.Manager.PassGen.Services;


import com.Passman.Manager.Vault.DTO.PasswordGenerationDTO;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class PasswordGeneratorService {
    private static final String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()-_=+[]{}|;:'\",.<>/?`~";

    private final SecureRandom random = new SecureRandom();


    public String generatePassword(PasswordGenerationDTO passwordGenerationDTO) {
        int length = passwordGenerationDTO.getLength();
        boolean includeLowercase = passwordGenerationDTO.isLowercase();
        boolean includeUppercase = passwordGenerationDTO.isUppercase();
        boolean includeDigits = passwordGenerationDTO.isDigits();
        boolean includeSpecialChars = passwordGenerationDTO.isSymbols();

        if (length <= 0) {
            throw new IllegalArgumentException("Длина пароля должна быть больше нуля.");
        }

        StringBuilder charPool = new StringBuilder();
        if (includeLowercase) {
            charPool.append(LOWERCASE_CHARS);
        }
        if (includeUppercase) {
            charPool.append(UPPERCASE_CHARS);
        }
        if (includeDigits) {
            charPool.append(DIGITS);
        }
        if (includeSpecialChars) {
            charPool.append(SPECIAL_CHARS);
        }

        if (charPool.isEmpty()) {
            throw new IllegalArgumentException("Необходимо выбрать хотя бы один тип символов для генерации пароля.");
        }

        StringBuilder password = new StringBuilder(length);
        String finalCharPool = charPool.toString();

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(finalCharPool.length());
            password.append(finalCharPool.charAt(randomIndex));
        }

        return password.toString();
    }

}
