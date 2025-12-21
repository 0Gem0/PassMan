package com.Passman.Manager.Auth.Services;


import com.Passman.Manager.Auth.DTO.LoginUserDTO;
import com.Passman.Manager.Auth.DTO.RegisterUserDto;
import com.Passman.Manager.Auth.Models.User;
import com.Passman.Manager.Auth.POJO.KdfParams;
import com.Passman.Manager.Auth.Repos.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.server.authentication.SwitchUserWebFilter;
import org.springframework.stereotype.Service;
import java.util.Base64;

import java.security.SecureRandom;

@Service
public class AuthService {

    private final AuthenticationManager authManager;

    private final UserRepository userRepository;

    @Autowired
    public AuthService(AuthenticationManager authManager, UserRepository userRepository) {
        this.authManager = authManager;
        this.userRepository = userRepository;
    }

    public void login(LoginUserDTO userDTO) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userDTO.getLogin(),
                        userDTO.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }


}
