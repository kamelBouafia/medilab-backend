package com.medilab.security;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@RequiredArgsConstructor
public class AuthenticatedUser implements UserDetails {

    @NonNull
    private final Long id;
    private final Long labId;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final String userType; // "staff" or "patient"
    private final boolean forcePasswordChange;
    private final boolean gdprAccepted;
    private final boolean enabled;

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
        return enabled;
    }
}
