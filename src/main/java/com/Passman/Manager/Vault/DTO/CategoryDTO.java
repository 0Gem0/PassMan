package com.Passman.Manager.Vault.DTO;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDTO {
    private String updatedName;

    public CategoryDTO(String updatedName) {
        this.updatedName = updatedName;
    }

    public CategoryDTO() {
    }
}
