package com.Passman.Manager.vault;



public record EntryView(
        Long id,
         Long userId,
         String title,
         String website,
         String email,
         String categoryName,
         String note,

        String passwordCipher,

         String passwordIv
) {

}
