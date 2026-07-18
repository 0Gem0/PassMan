package com.Passman.Manager.auth;

import com.Passman.Manager.shared.POJO.KdfParams;

import java.util.List;

public interface AuthApi {
    UserView getUserViewById(Long id);

    void initializeVault(Long userId, KdfParams kdfParams, String cryptoSalt, String publicKey, String encryptedPrivateKey, String privateKeyIv);

    List<UserView> findAllUsers();

    List<UserView> findUsersByDepartmentId(Long departmentId);

    void updateUserDepartment(Long userId, Long departmentId);
}
