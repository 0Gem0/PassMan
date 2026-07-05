package com.Passman.Manager.management_roles.internal.Repos;


import com.Passman.Manager.management_roles.internal.Models.AccessRights;
import com.Passman.Manager.management_roles.internal.Models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccessRightsRepository extends JpaRepository<AccessRights, Long> {
    Optional<AccessRights> findByRoleIdAndEntryId(Long roleId, Long entryId);

    List<AccessRights> findAllByEntryId(Long entryId);

    void deleteByRoleIdAndEntryId(Long roleId, Long entryId);

    @Query("""
        select case when count(ar) > 0 then true else false end
        from AccessRights ar
        where ar.entryId = :entryId
          and ar.role.id in (
              select ur.roleId
              from UsersRoles ur
              where ur.userId = :userId
          )
          and (ar.canView = true or ar.canEdit = true)
    """)
    boolean existsEffectiveRoleAccessForUser(
            @Param("userId") Long userId,
            @Param("entryId") Long entryId
    );

}
