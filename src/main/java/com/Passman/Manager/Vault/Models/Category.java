package com.Passman.Manager.Vault.Models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.Passman.Manager.Auth.Models.User;

@Getter
@Setter
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(nullable = false)
    private boolean system;

}
