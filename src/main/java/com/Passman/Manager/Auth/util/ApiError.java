package com.Passman.Manager.Auth.util;


import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

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
