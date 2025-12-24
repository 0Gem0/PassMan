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
    private String data;
    private String note;

    public EntryGetDTO(String title, String website, String email, String categoryName, String data, String note) {
        this.title = title;
        this.website = website;
        this.email = email;
        this.categoryName = categoryName;
        this.data = data;
        this.note = note;
    }
}
