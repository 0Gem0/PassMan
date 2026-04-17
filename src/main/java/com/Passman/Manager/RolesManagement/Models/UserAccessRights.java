package com.Passman.Manager.RolesManagement.Models;
import com.Passman.Manager.Auth.Models.User;
import com.Passman.Manager.Vault.Models.Entry;
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

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "entry_id", referencedColumnName = "id")
    private Entry entry;

    @Column(name = "can_view")
    private boolean canView;  // Может ли пользователь просматривать запись
    @Column(name = "can_edit")
    private boolean canEdit;  // Может ли пользователь редактировать запись
}
