package com.Passman.Manager.RolesManagement.Repos;

import com.Passman.Manager.Auth.Models.User;
import com.Passman.Manager.RolesManagement.Models.UserAccessRights;
import com.Passman.Manager.Vault.Models.Entry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserAccessRightsRepository extends JpaRepository<UserAccessRights, Long> {
    Optional<UserAccessRights> findByUserAndEntry(User user, Entry entry);
    Optional<UserAccessRights> findByUserAndEntryId(User user, Long entryId);

    List<UserAccessRights> findAllByEntryId(Long entryId);
    void deleteByUserAndEntry(User user, Entry entry);


    @Query("""
        select case when count(uar) > 0 then true else false end
        from UserAccessRights uar
        where uar.user.id = :userId
          and uar.entry.id = :entryId
          and (uar.canView = true or uar.canEdit = true)
    """)
    boolean existsEffectivePersonalAccess(
            @Param("userId") Long userId,
            @Param("entryId") Long entryId
    );
}
