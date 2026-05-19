package com.Passman.Manager.auth.internal.Services;

import com.Passman.Manager.auth.internal.DTO.RegisterUserDto;
import com.Passman.Manager.management_roles.ManagementRolesApi;
import com.Passman.Manager.management_roles.internal.Models.Role;
import com.Passman.Manager.auth.internal.Models.User;

import com.Passman.Manager.auth.internal.Repos.UserRepository;
import com.Passman.Manager.management_roles.internal.Repos.RoleRepository;
import com.Passman.Manager.shared.util.PasswordMismatchException;
import com.Passman.Manager.shared.util.UserAlreadyExistsException;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;

@Service
public class RegistrationService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;



    private final PasswordEncoder passwordEncoder;

    private final ModelMapper mapper;

    private final ManagementRolesApi managementRolesApi;


    public RegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder, ModelMapper mapper, RoleRepository roleRepository, ManagementRolesApi managementRolesApi) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
        this.managementRolesApi = managementRolesApi;
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
//        user.getRoles().add(roleUser);
        mapper.map(registerUserDto, user);
        user.setPassword(passwordEncoder.encode(registerUserDto.getPassword()));
//        roleUser.getUsers().add(user);
        managementRolesApi.addRoleToUserByName(user.getId(), "ROLE_USER");
        userRepository.save(user);
    }

}
