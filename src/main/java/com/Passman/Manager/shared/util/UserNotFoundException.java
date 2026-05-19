package com.Passman.Manager.shared.util;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String login) { super("Incorrect login or password " ); }
    public UserNotFoundException(Long id) { super("No user with id "  + id); }
}
