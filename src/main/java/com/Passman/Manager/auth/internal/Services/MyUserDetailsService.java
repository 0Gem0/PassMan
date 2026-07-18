package com.Passman.Manager.auth.internal.Services;
import com.Passman.Manager.auth.internal.Models.User;
import com.Passman.Manager.auth.internal.Repos.UserRepository;
import com.Passman.Manager.auth.internal.Security.MyUserDetails;
import com.Passman.Manager.management_roles.ManagementRolesApi;
import com.Passman.Manager.management_roles.RoleView;
import com.Passman.Manager.shared.util.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MyUserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepository;

    private final ManagementRolesApi managementRolesApi;


    @Autowired
    public MyUserDetailsService(UserRepository userRepository, @Lazy ManagementRolesApi managementRolesApi) {
        this.userRepository = userRepository;
        this.managementRolesApi = managementRolesApi;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByLogin(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
        List<RoleView> roles = managementRolesApi.findRolesByUserId(user.getId());
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .toList();
        return new MyUserDetails(user, authorities);
    }
}
