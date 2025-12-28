package com.medilab.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain chain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Claims claims = jwtUtil.extractAllClaims(jwt);
            Long userId = claims.get("userId", Long.class);
            Long labId = claims.get("labId", Long.class);

            // Read authorities safely
            Object authObj = claims.get("authorities");
            List<String> authoritiesList;
            if (authObj instanceof List) {
                authoritiesList = ((List<?>) authObj).stream().filter(Objects::nonNull).map(Object::toString)
                        .collect(Collectors.toList());
            } else {
                authoritiesList = Collections.emptyList();
            }

            String userType = claims.get("type", String.class);
            Boolean forceFlag = claims.get("forcePasswordChange", Boolean.class);
            boolean forcePwd = Boolean.TRUE.equals(forceFlag);

            AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                    userId,
                    labId,
                    username,
                    null, // Password is not needed in the context
                    authoritiesList.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()),
                    userType,
                    forcePwd,
                    true);

            if (jwtUtil.validateToken(jwt, authenticatedUser)) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        authenticatedUser, null, authenticatedUser.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }
}
