package com.Passman.Manager.RolesManagement.DTO;


import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.PrimitiveIterator;

@Getter
@Setter
public class UserPublicKeyDTO {

    private Long id;

    private String login;

    private String publicKey;

    private Long departmentId;

    private String departmentName;

    private List<RoleDTO> roles;
    public UserPublicKeyDTO() {
    }

    public UserPublicKeyDTO(String publicKey) {
        this.publicKey = publicKey;
    }

}