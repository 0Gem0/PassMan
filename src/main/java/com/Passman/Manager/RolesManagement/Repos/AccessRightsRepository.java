package com.Passman.Manager.RolesManagement.Repos;


import com.Passman.Manager.Auth.Models.User;
import com.Passman.Manager.RolesManagement.Models.AccessRights;
import com.Passman.Manager.RolesManagement.Models.Role;
import com.Passman.Manager.RolesManagement.Models.UserAccessRights;
import com.Passman.Manager.Vault.Models.Entry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccessRightsRepository extends JpaRepository<AccessRights, Long> {

    Optional<AccessRights> findByRoleAndEntryId(Role role, Long entryId);
    Optional<AccessRights> findByRoleAndEntry(Role role, Entry entry);

}
