package com.medilab.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class SecurityUtils {

    private SecurityUtils() {
        // Private constructor to hide the implicit public one
    }

    public static AuthenticatedUser getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser)) {
            throw new IllegalStateException("No authenticated user found in security context");
        }
        return (AuthenticatedUser) authentication.getPrincipal();
    }

    public static Optional<AuthenticatedUser> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser)) {
            return Optional.empty();
        }
        return Optional.of((AuthenticatedUser) authentication.getPrincipal());
    }
}
