package com.Passman.Manager.management_roles.internal.Models;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(name = "useraccessrights")
public class UserAccessRights {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "entry_id", nullable = false)
    private Long entryId;

    @Column(name = "can_view")
    private boolean canView;  // Может ли пользователь просматривать запись
    @Column(name = "can_edit")
    private boolean canEdit;  // Может ли пользователь редактировать запись
}
