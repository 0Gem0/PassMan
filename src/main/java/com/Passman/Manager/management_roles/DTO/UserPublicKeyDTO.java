package com.Passman.Manager.management_roles.DTO;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

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