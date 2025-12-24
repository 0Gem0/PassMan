package com.Passman.Manager.Vault.Models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import com.Passman.Manager.Auth.Models.User;

@Entity
@Getter
@Setter
@Table(name = "entries")
public class Entry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 50, nullable = false)
    private String title;

    @Column(length = 50)
    private String website;

    @Column(length = 50)
    private String email;

    @Column(length = 100)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "encrypted_data", nullable = false)
    private byte[] encryptedData;

    @Column(name = "encrypted_dek", nullable = false)
    private byte[] encryptedDek;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.createdAt = LocalDateTime.now();
    }
}