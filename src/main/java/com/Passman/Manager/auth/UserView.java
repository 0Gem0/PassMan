package com.Passman.Manager.auth;

import com.Passman.Manager.shared.POJO.KdfParams;

import java.time.LocalDateTime;

public record UserView(
        Long id,
        String login,
        Long departmentId,
        String publicKey,
        String cryptoSalt,
        KdfParams kdfParams,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String password,
        boolean vaultInitialized,
        String encryptedPrivateKey,
        String privateKeyIv
) {
}