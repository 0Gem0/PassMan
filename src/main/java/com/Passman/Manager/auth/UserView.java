package com.Passman.Manager.auth;

public record UserView(
        Long id,
        String login,
        Long departmentId,
        String publicKey
) {
}