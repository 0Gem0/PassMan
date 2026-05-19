package com.Passman.Manager.management_roles.internal;


import com.Passman.Manager.management_roles.ManagementRolesApi;
import com.Passman.Manager.management_roles.RoleView;
import com.Passman.Manager.management_roles.internal.Models.Role;
import com.Passman.Manager.management_roles.internal.Models.UsersRoles;
import com.Passman.Manager.management_roles.internal.Repos.RoleRepository;
import com.Passman.Manager.management_roles.internal.Repos.UsersRolesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class ManagementRolesApiImplementation implements ManagementRolesApi {

    private final RoleRepository roleRepository;
    private final UsersRolesRepository usersRolesRepository;

    @Autowired
    public ManagementRolesApiImplementation(RoleRepository roleRepository, UsersRolesRepository usersRolesRepository) {
        this.roleRepository = roleRepository;
        this.usersRolesRepository = usersRolesRepository;
    }

    @Override
    public void addRoleToUserByName(Long userId, String roleName) {
        Optional<Long> roleId = roleRepository.findIdByName(roleName);
        if (roleId.isPresent()){
            usersRolesRepository.save(new UsersRoles(userId, roleId.get()));
        }
        else throw new RuntimeException("No such role");
    }

//    @Override
//    public RoleView findByName(String name) {
//        Optional<Role> roleOptional = roleRepository.findByName(name);
//        if (roleOptional.isPresent()){
//            return roleOptional.get();
//        }
//        else{
//
//        }
//    }

}
