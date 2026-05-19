package com.Passman.Manager.auth.internal;

import com.Passman.Manager.auth.AuthApi;
import com.Passman.Manager.auth.UserView;
import com.Passman.Manager.auth.internal.Models.User;
import com.Passman.Manager.auth.internal.Repos.UserRepository;
import com.Passman.Manager.shared.util.UserNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


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
    public UserView getUserById(Long id) {
        Optional <User> userOptional = userRepository.findById(id);
       if (userOptional.isPresent()){
           User user = userOptional.get();
           return new UserView(
                   user.getId(),
                   user.getLogin(),
                   user.getDepartment() != null ? user.getDepartment().getId() : null,
                   user.getPublicKey()
           );
       }
        else throw new UserNotFoundException(id);
    }

    @Override
    public UserView getUserView(Long userId) {
        return null;
    }

    @Override
    public String getPublicKey(Long userId) {
        return null;
    }

    @Override
    public boolean userExists(Long userId) {
        return false;
    }
}
