package com.Passman.Manager.Vault.DTO;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryCountDTO {
    private final String categoryName;
    private final long countEntries;

    public CategoryCountDTO(String categoryName, long countEntries) {
        this.categoryName = categoryName;
        this.countEntries = countEntries;
    }

}