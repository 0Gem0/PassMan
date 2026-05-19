package com.Passman.Manager.management_roles.internal.Repos;

import com.Passman.Manager.management_roles.internal.Models.UserAccessRights;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserAccessRightsRepository extends JpaRepository<UserAccessRights, Long> {
//    Optional<UserAccessRights> findByUserIdAndEntryId(User user, Entry entry);
    Optional<UserAccessRights> findByUserIdAndEntryId(Long userId, Long entryId);

    List<UserAccessRights> findAllByEntryId(Long entryId);
    void deleteByUserIdAndEntryId(Long userId, Long entryId);


    @Query("""
        select case when count(uar) > 0 then true else false end
        from UserAccessRights uar
        where uar.userId = :userId
          and uar.entryId = :entryId
          and (uar.canView = true or uar.canEdit = true)
    """)
    boolean existsEffectivePersonalAccess(
            @Param("userId") Long userId,
            @Param("entryId") Long entryId
    );
}
