package com.Passman.Manager.RolesManagement.Models;

import com.Passman.Manager.Auth.Models.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "departments")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "department")
    private List<Role> roles;

    @OneToMany(mappedBy = "department")
    private List<User> users;
}