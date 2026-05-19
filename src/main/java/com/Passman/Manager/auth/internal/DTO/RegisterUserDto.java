package com.Passman.Manager.auth.internal.DTO;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterUserDto {

    private String login;
    private String password;
    private String passwordConfirm;

    public RegisterUserDto(String login, String password, String passwordConfirm) {
        this.login = login;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
    }
}
