package com.Passman.Manager.auth.internal.Services;

import com.Passman.Manager.auth.internal.DTO.RegisterUserDto;
import com.Passman.Manager.management_roles.ManagementRolesApi;
import com.Passman.Manager.auth.internal.Models.User;

import com.Passman.Manager.auth.internal.Repos.UserRepository;
import com.Passman.Manager.shared.util.DuplicateResourceException;
import com.Passman.Manager.shared.util.NotValidException;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class RegistrationService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final ModelMapper mapper;

    private final ManagementRolesApi managementRolesApi;


    public RegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder, ModelMapper mapper, ManagementRolesApi managementRolesApi) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
        this.managementRolesApi = managementRolesApi;
    }

    @Transactional
    public void register(RegisterUserDto registerUserDto) {
        Optional<User> userOptional = userRepository.findUserByLogin(registerUserDto.getLogin());
        if (userOptional.isPresent()){
            throw new DuplicateResourceException("User already exists");
        }
        if (!registerUserDto.getPassword().equals(registerUserDto.getPasswordConfirm())){
            throw new NotValidException("Passwords do not match");
        }

        User user = new User();
        mapper.map(registerUserDto, user);
        user.setPassword(passwordEncoder.encode(registerUserDto.getPassword()));
        managementRolesApi.addRoleToUserByName(user.getId(), "ROLE_USER");
        userRepository.save(user);
    }

}
