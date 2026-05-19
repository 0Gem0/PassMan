package com.Passman.Manager.vault.DTO;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntryGetDTO {
    private String title;
    private String website;
    private String email;
    private String categoryName;
    private String note;

    private String passwordCipher;
    private String passwordIv;

    private String encryptedDek;
    private String dekIv;
    private String dekEnvelopeType;

    public EntryGetDTO() {
    }
}
