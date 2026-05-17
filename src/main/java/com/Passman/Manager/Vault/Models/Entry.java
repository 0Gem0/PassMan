package com.Passman.Manager.Vault.Models;


import com.Passman.Manager.RolesManagement.Models.AccessRights;
import com.Passman.Manager.RolesManagement.Models.UserAccessRights;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

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

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 255)
    private String website;

    @Column(length = 255)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "password_cipher", columnDefinition = "TEXT", nullable = false)
    private String passwordCipher;

    @Column(name = "password_iv", nullable = false)
    private String passwordIv;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "entry")
    private List<AccessRights> accessRights;

    @OneToMany(mappedBy = "entry")
    private List<UserAccessRights> userAccessRights;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}