package com.medilab.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import java.io.IOException;

@Configuration
public class PrivateNetworkAccessConfig {

    @Bean
    public FilterRegistrationBean<PrivateNetworkAccessFilter> privateNetworkAccessFilter() {
        FilterRegistrationBean<PrivateNetworkAccessFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new PrivateNetworkAccessFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1); // Execute before other filters
        return registrationBean;
    }

    private static class PrivateNetworkAccessFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {

            // Add Private Network Access headers
            response.setHeader("Access-Control-Allow-Private-Network", "true");

            // Handle preflight requests
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
                response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
                response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept, Origin");
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setHeader("Access-Control-Max-Age", "3600");
                response.setStatus(HttpServletResponse.SC_OK);
                return;
            }

            filterChain.doFilter(request, response);
        }
    }
}