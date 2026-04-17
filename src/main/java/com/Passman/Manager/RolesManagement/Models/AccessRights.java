package com.Passman.Manager.RolesManagement.Models;

import com.Passman.Manager.Vault.Models.Entry;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(name = "accessrights")
public class AccessRights {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "role_id", referencedColumnName = "id")
    private Role role;

    @ManyToOne
    @JoinColumn(name = "entry_id", referencedColumnName = "id")
    private Entry entry;

    @Column(name = "can_view")
    private boolean canView;  // Может ли пользователь просматривать запись
    @Column(name = "can_edit")
    private boolean canEdit;
}