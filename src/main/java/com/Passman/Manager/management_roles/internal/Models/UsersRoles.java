package com.Passman.Manager.management_roles.internal.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users_roles")
public class UsersRoles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "role_id")
    private Long roleId;

    public UsersRoles(Long userId, Long roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    public UsersRoles() {
    }
}