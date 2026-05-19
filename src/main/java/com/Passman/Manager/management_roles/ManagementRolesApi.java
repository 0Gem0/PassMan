package com.Passman.Manager.management_roles;

import com.Passman.Manager.management_roles.internal.Models.Role;

public interface ManagementRolesApi {

//    Role findByName(String name);
    void addRoleToUserByName(Long userId, String roleName);

}
