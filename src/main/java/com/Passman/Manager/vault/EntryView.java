package com.Passman.Manager.vault;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public record EntryView(
        Long id,
         Long userId,
         String title,
         String website,
         String email,
         String categoryName,
         String note,

        /**
         * Зашифрованный пароль записи (base64)
         */
        String passwordCipher,

        /**
         * IV для passwordCipher (base64)
         */
         String passwordIv
) {

}
