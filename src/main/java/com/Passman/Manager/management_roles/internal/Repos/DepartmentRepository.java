package com.Passman.Manager.management_roles.internal.Repos;

import com.Passman.Manager.management_roles.internal.Models.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

}
