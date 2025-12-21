package com.Passman.Manager.Auth.util;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String login) { super("Incorrect login or password " ); }
}
