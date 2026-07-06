package com.Passman.Manager.shared.Security;

import com.Passman.Manager.auth.internal.Models.User;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;

@Getter
public class MyUserDetails implements UserDetails {

    private final User user;

    private final Collection<? extends GrantedAuthority> authorities;

    public MyUserDetails(User user, Collection<? extends GrantedAuthority> authorities) {
        this.user = user;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getLogin();
    }

    public Long getId() {
        return user.getId();
    }
    @Override
    public boolean isAccountNonExpired() {
        return user.getAccountExpiration() == null
                || LocalDateTime.now().isBefore(user.getAccountExpiration());
    }

    @Override
    public boolean isAccountNonLocked() {
        if (user.getLockExpiration() != null) {
            if (LocalDateTime.now().isAfter(user.getLockExpiration())) {
                user.setLockExpiration(null);
                user.setFailedLoginAttempts(0);
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return user.getCredentialsExpiration() == null
                || LocalDateTime.now().isBefore(user.getCredentialsExpiration());
    }


    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }


}
