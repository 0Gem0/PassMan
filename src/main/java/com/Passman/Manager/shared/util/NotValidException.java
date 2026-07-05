package com.Passman.Manager.shared.util;

public class NotValidException extends RuntimeException {

    public NotValidException(String message) {
        super(message);
    }
}