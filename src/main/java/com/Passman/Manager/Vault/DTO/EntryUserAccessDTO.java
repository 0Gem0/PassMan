package com.Passman.Manager.Vault.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EntryUserAccessDTO {
    private Long userId;
    private String login;
    private boolean canView;
    private boolean canEdit;
}