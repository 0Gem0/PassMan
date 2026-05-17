package com.Passman.Manager.Vault.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EntryRoleAccessDTO {
    private Long roleId;
    private String roleName;
    private boolean canView;
    private boolean canEdit;
}