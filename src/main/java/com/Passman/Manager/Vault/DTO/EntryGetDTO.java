package com.Passman.Manager.Vault.DTO;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntryGetDTO {
    private String title;
    private String website;
    private String email;
    private String categoryName;
    private byte[] passwordCipher;
    private byte[] passwordIv;
    private String note;

    public EntryGetDTO() {
    }
}
