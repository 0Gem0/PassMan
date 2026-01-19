package com.Passman.Manager.Vault.DTO;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
    public class EntryDTO {

        private long id;
        private String title;
        private String website;
        private String email;
        private String categoryName;
        private byte[] password;
        private byte[] passwordIv;
        private String note;


    public EntryDTO(){

        }
    }
