package com.Passman.Manager.vault.internal.Models;//package com.Passman.Manager.Vault.Models;
//
//import com.Passman.Manager.RolesManagement.Models.Role;
//import jakarta.persistence.*;
//
//import java.util.List;
//
//@Entity
//public class Department {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String name;  // Название отдела, например, "HR", "IT", "Sales"
//
//    @OneToMany(mappedBy = "department")
//    private List<Role> roles;  // Роли, связанные с этим отделом
//}