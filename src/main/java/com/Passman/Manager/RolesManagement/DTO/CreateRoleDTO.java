package com.Passman.Manager.RolesManagement.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRoleDTO {
    private String name;
    private Long departmentId;
}