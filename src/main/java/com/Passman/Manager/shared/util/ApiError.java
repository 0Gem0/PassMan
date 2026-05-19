package com.Passman.Manager.shared.util;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiError {
    private String message;
    private String exceptionMessage;

    public ApiError(String message, String exceptionMessage) {
        this.message = message;
        this.exceptionMessage = exceptionMessage;
    }
}
