package com.Passman.Manager.auth.internal;

import com.Passman.Manager.auth.AuthApi;
import com.Passman.Manager.auth.UserView;
import com.Passman.Manager.auth.internal.Models.User;
import com.Passman.Manager.auth.internal.Repos.UserRepository;
import com.Passman.Manager.shared.POJO.KdfParams;
import com.Passman.Manager.shared.util.DuplicateResourceException;
import com.Passman.Manager.shared.util.NotFoundException;
import com.Passman.Manager.shared.util.NotValidException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class AuthApiImplementation implements AuthApi {


    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public AuthApiImplementation(UserRepository userRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public UserView getUserViewById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
       if (userOptional.isPresent()){
           User user = userOptional.get();
           return modelMapper.map(user, UserView.class);
       }
        else throw new NotValidException("User with id" + id + "not found");
    }

    @Override
    @Transactional
    public void initializeVault(Long userId, KdfParams kdfParams, String cryptoSalt, String publicKey, String encryptedPrivateKey,String privateKeyIv) {
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.isVaultInitialized()) {
            throw new DuplicateResourceException("Vault already initialized");
        }

        user.setVaultInitialized(true);
        user.setKdfParams(kdfParams);
        user.setCryptoSalt(cryptoSalt);
        user.setPublicKey(publicKey);
        user.setEncryptedPrivateKey(encryptedPrivateKey);
        user.setPrivateKeyIv(privateKeyIv);
    }

    @Override
    public List<UserView> findAllUsers() {
        return userRepository.findAll().stream().map(user -> modelMapper.map(user, UserView.class)).collect(Collectors.toList());
    }

    @Override
    public List<UserView> findUsersByDepartmentId(Long departmentId) {
        return userRepository.findAllByDepartmentId(departmentId).stream().map(user -> modelMapper.map(user, UserView.class)).collect(Collectors.toList());
    }

    @Override
    public void updateUserDepartment(Long userId, Long departmentId) {
        Optional<User> userOptional = userRepository.findUserById(userId);
        if (userOptional.isPresent()){
            User user = userOptional.get();
            user.setDepartmentId(departmentId);
        }
        else{
            throw new NotValidException("User not found with id" + userId);
        }
    }
}
