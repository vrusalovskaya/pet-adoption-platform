package com.wise.petadoption.security.integration;

import com.wise.petadoption.security.domain.SecurityUser;
import com.wise.petadoption.user.domain.User;
import com.wise.petadoption.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userService.findByEmail(email);
        return SecurityUser.from(user);
    }
}
