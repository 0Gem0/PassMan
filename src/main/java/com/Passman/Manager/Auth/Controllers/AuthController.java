package com.Passman.Manager.Auth.Controllers;

import com.Passman.Manager.Auth.DTO.LoginUserDTO;
import com.Passman.Manager.Auth.DTO.RegisterUserDto;
import com.Passman.Manager.Auth.Services.AuthService;
import com.Passman.Manager.Auth.Services.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RequestMapping("/auth")
@RestController
public class AuthController {


    private final RegistrationService registrationService;

    private final AuthService authService;

    @Autowired
    public AuthController(RegistrationService registrationService, AuthService authService) {
        this.registrationService = registrationService;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> processLoginPage(@RequestBody LoginUserDTO loginUserDTO){
        authService.login(loginUserDTO);
        return ResponseEntity.ok("logged in");
    }

    @PostMapping("/register")
    public ResponseEntity<?> processRegistrationPage(@RequestBody RegisterUserDto registerUserDto){
        System.out.println(registerUserDto.getLogin());
        registrationService.register(registerUserDto);
        return ResponseEntity.ok("registered");
    }

}
