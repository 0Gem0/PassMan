package com.Passman.Manager.Auth.DTO;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginUserDTO {

    private String login;
    private String password;

    public LoginUserDTO(String login, String password) {
        this.login = login;
        this.password = password;
    }
}
