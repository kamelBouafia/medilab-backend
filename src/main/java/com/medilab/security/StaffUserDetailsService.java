package com.medilab.security;

import com.medilab.entity.StaffUser;
import com.medilab.repository.StaffUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
public class StaffUserDetailsService implements UserDetailsService {

    private final StaffUserRepository staffUserRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        StaffUser staffUser = staffUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return new AuthenticatedUser(
                staffUser.getId(),
                staffUser.getLab().getId(),
                staffUser.getUsername(),
                staffUser.getPassword(),
                List.of(new SimpleGrantedAuthority(staffUser.getRole().name())),
                "staff"
        );
    }
}
