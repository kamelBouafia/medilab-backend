package com.medilab.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class AuthenticatedUser implements UserDetails {

    private final Long id;
    private final Long labId;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final String userType; // "staff" or "patient"

    public AuthenticatedUser(Long id, Long labId, String username, String password, Collection<? extends GrantedAuthority> authorities, String userType) {
        this.id = id;
        this.labId = labId;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.userType = userType;
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
}
