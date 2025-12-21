package com.Passman.Manager.Auth.Repos;

import com.Passman.Manager.Auth.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByLogin(String login);

    User findUserById(long id);

}
