package com.Passman.Manager.RolesManagement.DTO;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignRoleDTO {
    private Long targetUserId;
    private Long roleId;

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}