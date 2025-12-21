package com.Passman.Manager.Auth.util;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ApiError> handlePasswords(PasswordMismatchException ex) {
        return ResponseEntity.badRequest().body(new ApiError("PASSWORD_MISMATCH", ex.getMessage()));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleExists(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError("USER_EXISTS", ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleExists(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError("USER_NOT_FOUND", ex.getMessage()));
    }
}
