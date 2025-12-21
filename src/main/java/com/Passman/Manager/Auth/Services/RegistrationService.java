package com.Passman.Manager.Auth.Services;

import com.Passman.Manager.Auth.DTO.RegisterUserDto;
import com.Passman.Manager.Auth.Models.Role;
import com.Passman.Manager.Auth.Models.User;
import com.Passman.Manager.Auth.POJO.KdfParams;
import com.Passman.Manager.Auth.Repos.RoleRepository;
import com.Passman.Manager.Auth.Repos.UserRepository;
import com.Passman.Manager.Auth.util.PasswordMismatchException;
import com.Passman.Manager.Auth.util.UserAlreadyExistsException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class RegistrationService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final ModelMapper mapper;
    private final RoleRepository roleRepository;

    public RegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder, ModelMapper mapper, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public void register(RegisterUserDto registerUserDto) {
        Optional<User> userOptional = userRepository.findUserByLogin(registerUserDto.getLogin());
        if (userOptional.isPresent()){
            throw new UserAlreadyExistsException();
        }
        if (!registerUserDto.getPassword().equals(registerUserDto.getPasswordConfirm())){
            throw new PasswordMismatchException();
        }

        User user = new User();
        Role roleUser = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role USER not found"));
        user.getRoles().add(roleUser);
        mapper.map(registerUserDto, user);
        user.setPassword(passwordEncoder.encode(registerUserDto.getPassword()));
        roleUser.getUsers().add(user);
        userRepository.save(enrichUser(user));
    }

    public String createSalt(int length){
        byte[] salt = new byte[length];
        SECURE_RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public KdfParams setParams(){
        KdfParams params = new KdfParams();
        params.setMemoryMb(65536);
        params.setIterations(10);
        params.setParallelism(2);
        params.setDkLen(32);
        return params;
    }

    public User enrichUser(User user){
        user.setKdfParams(setParams());
        user.setSalt(createSalt(32));
//        user.setCompany("Company");
        return user;
    }
}
