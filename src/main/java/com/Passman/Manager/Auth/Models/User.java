package com.Passman.Manager.Auth.Models;


import com.Passman.Manager.Auth.POJO.KdfParams;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
    private String salt;

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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "users_role",
                joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
                inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private Set<Role> roles = new HashSet<>();


    @Column(name = "encrypted_dek")
    private byte[] encryptedDek;

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
