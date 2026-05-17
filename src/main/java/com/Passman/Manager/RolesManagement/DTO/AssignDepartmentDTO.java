package com.Passman.Manager.RolesManagement.DTO;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignDepartmentDTO {
    private Long targetUserId;
    private Long departmentId;

}