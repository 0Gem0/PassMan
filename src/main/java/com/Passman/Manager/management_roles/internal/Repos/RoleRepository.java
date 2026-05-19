package com.Passman.Manager.management_roles.internal.Repos;


import com.Passman.Manager.management_roles.internal.Models.Department;
import com.Passman.Manager.management_roles.internal.Models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    Optional<Role> findById(Long id);

    Optional<Long> findIdByName(String name);
    boolean existsByNameAndDepartmentId(String name, Long departmentId);
    List<Role> findByDepartmentId(Long departmentId);
}
