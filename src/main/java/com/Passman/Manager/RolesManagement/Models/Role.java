package com.Passman.Manager.RolesManagement.Models;

import com.Passman.Manager.Auth.Models.User;
import com.Passman.Manager.RolesManagement.Models.Department;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;


@Getter
@Setter
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "department_id", referencedColumnName = "id")
    private Department department;

    @OneToMany(mappedBy = "role")
    private List<AccessRights> accessRights;

    @ManyToMany(mappedBy = "roles")
    private List<User> users;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role role)) return false;
        return Objects.equals(id, role.id) && Objects.equals(name, role.name) && Objects.equals(department, role.department) && Objects.equals(accessRights, role.accessRights) && Objects.equals(users, role.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, department, accessRights, users);
    }
}