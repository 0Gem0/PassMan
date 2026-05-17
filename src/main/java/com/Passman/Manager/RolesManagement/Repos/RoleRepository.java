package com.Passman.Manager.RolesManagement.Repos;


import com.Passman.Manager.RolesManagement.Models.Department;
import com.Passman.Manager.RolesManagement.Models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    Optional<Role> findById(Long id);

    boolean existsByNameAndDepartmentId(String name, Long departmentId);
    List<Role> findByDepartment(Department department);
}
