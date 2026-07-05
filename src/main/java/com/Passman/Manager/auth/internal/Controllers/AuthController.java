package com.Passman.Manager.auth.internal.Controllers;

import com.Passman.Manager.auth.internal.DTO.LoginUserDTO;
import com.Passman.Manager.auth.internal.DTO.RegisterUserDto;
import com.Passman.Manager.auth.internal.Security.MyUserDetails;
import com.Passman.Manager.auth.internal.Services.AuthService;
import com.Passman.Manager.auth.internal.Services.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @GetMapping("/check")
    public ResponseEntity<?> check(@AuthenticationPrincipal MyUserDetails user) {
        if (user != null) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(401).build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> processLoginPage(@RequestBody LoginUserDTO loginUserDTO){
        authService.login(loginUserDTO);
        return ResponseEntity.ok("logged in");
    }

    @PostMapping("/register")
    public ResponseEntity<?> processRegistrationPage(@RequestBody RegisterUserDto registerUserDto){
        registrationService.register(registerUserDto);
        return ResponseEntity.ok("registered");
    }

}
