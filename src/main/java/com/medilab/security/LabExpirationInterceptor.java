package com.medilab.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Component
public class LabExpirationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String method = request.getMethod();

        // Only check for data-modifying requests
        if ("GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        try {
            AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();

            // System Admins are exempt from this check to allow them to manage labs
            if (user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SYSTEM_ADMIN"))) {
                return true;
            }

            if (user.getTrialEnd() != null && user.getTrialEnd().isBefore(LocalDateTime.now())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"message\": \"Your laboratory license has expired. Please contact support or upgrade.\", \"code\": \"EXPIRED\"}");
                return false;
            }
        } catch (IllegalStateException e) {
            // No authenticated user, let Spring Security handle it
            return true;
        }

        return true;
    }
}
