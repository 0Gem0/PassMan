package com.Passman.Manager.auth;

import com.Passman.Manager.vault.DTO.CryptoDTO;

import java.util.List;

public interface AuthApi {
    UserView getUserViewById(Long id);

    void initializeVault(Long userId, CryptoDTO cryptoDTO);

    String getPublicKey(Long userId);

    boolean userExists(Long userId);

    List<UserView> findAllUsers();

    List<UserView> findUsersByDepartmentId(Long departmentId);

    void updateUserDepartment(Long userId, Long departmentId);
}
