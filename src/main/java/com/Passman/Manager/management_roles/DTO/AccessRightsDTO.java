package com.Passman.Manager.management_roles.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccessRightsDTO {
    private Long entryId;
    private Long roleId;
    private boolean canView;
    private boolean canEdit;

    public AccessRightsDTO(Long entryId, Long roleId, boolean canView, boolean canEdit) {
        this.entryId = entryId;
        this.roleId = roleId;
        this.canView = canView;
        this.canEdit = canEdit;
    }
    public AccessRightsDTO(){

    }
}
