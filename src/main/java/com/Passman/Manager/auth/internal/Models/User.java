package com.Passman.Manager.auth.internal.Models;


import com.Passman.Manager.shared.POJO.KdfParams;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Size(min = 4, max = 100, message = "Длина логина от 4 символов")
    @Column(name = "login")
    private String login;

    @Column(name = "salt")
    private String cryptoSalt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "kdf_params", columnDefinition = "jsonb")
    private KdfParams kdfParams;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @NotEmpty(message = "Empty password")
    @Column(name = "password")
    private String password;

//    @Column(name = "company")
//    private String company;

    private Long departmentId;

    @Column(name = "vault_initialized", nullable = false)
    private boolean vaultInitialized = false;

    @Column(name = "public_key", columnDefinition = "TEXT")
    private String publicKey;

    @Column(name = "encrypted_private_key", columnDefinition = "TEXT")
    private String encryptedPrivateKey;

    @Column(name = "private_key_iv")
    private String privateKeyIv;

    @Column(nullable = false)
    private boolean enabled = false;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    @Column(name = "lock_expiration")
    private LocalDateTime lockExpiration;

    @Column(name = "credentials_expiration")
    private LocalDateTime credentialsExpiration;

    @Column(name = "account_expiration")
    private LocalDateTime accountExpiration;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public User(){

    }
}
