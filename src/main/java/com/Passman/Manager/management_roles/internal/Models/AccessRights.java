package com.Passman.Manager.management_roles.internal.Models;

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

    private Long entryId;

    @Column(name = "can_view")
    private boolean canView;  // Может ли пользователь просматривать запись
    @Column(name = "can_edit")
    private boolean canEdit;
}