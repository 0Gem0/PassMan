package com.Passman.Manager.auth;

import java.util.List;

public interface AuthApi {
    UserView getUserById(Long id);

    UserView getUserView(Long userId);

    String getPublicKey(Long userId);

    boolean userExists(Long userId);

    List<UserView> findAllUsers();

    List<UserView> findUsersByDepartmentId(Long departmentId);

    void updateUserDepartment(Long userId, Long departmentId);
}
