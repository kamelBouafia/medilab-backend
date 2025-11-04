package com.medilab.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@RequiredArgsConstructor
@Getter
public class TenantPrincipal implements Authentication {

    private final String name;
    private final String role;
    private final String labId;
    private boolean authenticated = true;
    private final Map<String, Object> claims;

    public static TenantPrincipal fromClaims(io.jsonwebtoken.Claims claims) {
        String name = claims.get("name", String.class);
        String role = claims.get("role", String.class);
        String labId = claims.get("labId", String.class);
        return new TenantPrincipal(name, role, labId, claims);
    }

    public String getUserId() {
        return (String) claims.get("userId"); // might be null for patient tokens
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public Object getCredentials() { return null; }

    @Override
    public Object getDetails() { return claims; }

    @Override
    public Object getPrincipal() { return name; }

    @Override
    public boolean isAuthenticated() { return authenticated; }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException { this.authenticated = isAuthenticated; }

}
