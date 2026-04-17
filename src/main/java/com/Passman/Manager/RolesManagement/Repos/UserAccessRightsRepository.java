package com.Passman.Manager.RolesManagement.Repos;

import com.Passman.Manager.Auth.Models.User;
import com.Passman.Manager.RolesManagement.Models.UserAccessRights;
import com.Passman.Manager.Vault.Models.Entry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccessRightsRepository extends JpaRepository<UserAccessRights, Long> {
    Optional<UserAccessRights> findByUserAndEntry(User user, Entry entry);
    Optional<UserAccessRights> findByUserAndEntryId(User user, Long entryId);
    void deleteByUserAndEntry(User user, Entry entry);
}
