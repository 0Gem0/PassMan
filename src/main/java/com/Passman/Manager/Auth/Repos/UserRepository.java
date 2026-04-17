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
    List<User> findByDepartment(Department department);
    @Query(value = "select u.encrypted_dek from users u where u.id = :id", nativeQuery = true)
    Optional<byte[]> findUserDek(@Param("id") long id);

    User findUserById(long id);

}
