package com.Passman.Manager.Auth.util;

public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException() { super("Passwords do not match"); }
}
