package com.Passman.Manager.Auth.Repos;

import com.Passman.Manager.Auth.Models.User;
import com.Passman.Manager.RolesManagement.Models.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByLogin(String login);
    List<User> findByDepartmentId(Long departmentId);

    User findUserById(long id);

    List<User> findAllByDepartmentId(Long id);

    List<User> findAllByDepartmentIdIsNull();

    List<User> findAllByDepartmentIdOrDepartmentIdIsNull(Long departmentId);

    User findUserById(Long id);

    @Query("""
        select distinct u
        from User u
        join u.roles r
        where r.id = :roleId
    """)
    List<User> findUsersByRoleId(@Param("roleId") Long roleId);

}
