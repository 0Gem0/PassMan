package com.Passman.Manager.Vault.DTO;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
    public class EntryDTO {
        private String title;
        private String website;
        private String email;
        private String categoryName;
        private String data;

        public EntryDTO(String title, String website, String email, String categoryName, String data) {
            this.title = title;
            this.website = website;
            this.email = email;
            this.categoryName = categoryName;
            this.data = data;
        }
        public EntryDTO(){

        }
    }
