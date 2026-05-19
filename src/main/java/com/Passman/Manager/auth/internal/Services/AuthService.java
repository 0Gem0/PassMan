package com.Passman.Manager.auth.internal.Services;


import com.Passman.Manager.auth.internal.DTO.LoginUserDTO;
import com.Passman.Manager.auth.internal.Repos.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


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
