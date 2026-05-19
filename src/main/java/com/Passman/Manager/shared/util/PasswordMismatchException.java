package com.Passman.Manager.shared.util;

public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException() { super("Passwords do not match"); }
}
