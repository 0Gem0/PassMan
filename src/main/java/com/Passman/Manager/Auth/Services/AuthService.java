package com.Passman.Manager.Auth.Services;


import com.Passman.Manager.Auth.DTO.LoginUserDTO;
import com.Passman.Manager.Auth.Repos.UserRepository;
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

//    public void setCryptoMeta(CryptoDTO cryptoDTO, Long userId){
//        User user = userRepository.findUserById(userId);
//        user.setKdfParams(cryptoDTO.getKdfParams());
//        user.setEncryptedDek(cryptoDTO.getEncryptedDek());
//    }


}
