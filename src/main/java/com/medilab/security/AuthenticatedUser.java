package com.medilab.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class AuthenticatedUser implements UserDetails {

    private final Long userId;
    private final Long labId;
    private final String role;
    private final Collection<? extends GrantedAuthority> authorities;

    public AuthenticatedUser(Long userId, Long labId, String role) {
        this.userId = userId;
        this.labId = labId;
        this.role = role;
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getUsername() {
        return String.valueOf(userId); // UserDetails requires String username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getPassword() {
        return null; // Not used for JWT
    }
}
