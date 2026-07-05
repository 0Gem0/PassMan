package com.Passman.Manager.management_roles.internal.Repos;


import com.Passman.Manager.management_roles.internal.Models.Role;
import com.Passman.Manager.management_roles.internal.Models.UsersRoles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsersRolesRepository extends JpaRepository<UsersRoles, Long> {

    List<Long> findAllByUserId(Long userId);

    @Query("select ur.userId from UsersRoles ur where ur.roleId = :roleId")
    List<Long> findUsersIdsByRoleId(Long roleId);

    @Query("select ur.roleId from UsersRoles ur where ur.userId = :userId")
    List<Long> findRolesIdsByUserId(Long userId);

    boolean existsByUserIdAndRoleId(Long userId, Long roleId);

    @Query("""
        select count(ur) > 0
        from UsersRoles ur
        join Role r on r.id = ur.roleId
        where ur.userId = :userId
          and lower(r.name) = lower(:roleName)
        """)
    boolean existsByUserIdAndRoleNameIgnoreCase(
            @Param("userId") Long userId,
            @Param("roleName") String roleName
    );

    void deleteByUserIdAndRoleId(Long userId, Long roleId);

}
