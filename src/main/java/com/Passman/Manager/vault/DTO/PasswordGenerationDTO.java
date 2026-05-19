package com.Passman.Manager.vault.DTO;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordGenerationDTO {
    private int length;
    private boolean isUppercase;
    private boolean isLowercase;
    private boolean isDigits;
    private boolean isSymbols;
}
