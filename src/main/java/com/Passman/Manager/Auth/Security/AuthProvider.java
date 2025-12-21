//package com.Passman.Manager.Auth.Security;
//
//import com.Passman.Manager.Auth.Services.MyUserDetailsService;
//import org.jspecify.annotations.Nullable;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Component;
//
//import java.util.Collections;
//import java.util.Objects;
//
//
//@Component
//public class AuthProvider implements AuthenticationProvider {
//
//
//    private final MyUserDetailsService myUserDetailsService;
//
//    @Autowired
//    public AuthProvider(MyUserDetailsService myUserDetailsService) {
//        this.myUserDetailsService = myUserDetailsService;
//    }
//
//    @Override
//    public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {
//        UserDetails myUserDetails = myUserDetailsService.loadUserByUsername(authentication.getName());
//        String pass = (authentication.getCredentials()).toString();
//        System.out.println(pass);
//
//        if (!Objects.equals(myUserDetails.getPassword(), pass)){
//            throw new BadCredentialsException("Ты балда");
//        }
//        return new UsernamePasswordAuthenticationToken(myUserDetails, pass, Collections.emptyList());
//    }
//
//    @Override
//    public boolean supports(Class<?> authentication) {
//        return true;
//    }
//}
