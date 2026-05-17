package com.Passman.Manager.RolesManagement.Repos;

import com.Passman.Manager.Auth.Models.User;
import com.Passman.Manager.RolesManagement.Models.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

}
