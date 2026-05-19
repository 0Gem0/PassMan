package com.Passman.Manager.management_roles.DTO;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAccessRightsDTO {
    private Long targetUserId;
    private Long entryId;
    private boolean canView;
    private boolean canEdit;
}