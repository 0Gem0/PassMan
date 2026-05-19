package com.Passman.Manager.management_roles.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRoleDTO {
    private String name;
    private Long departmentId;
}