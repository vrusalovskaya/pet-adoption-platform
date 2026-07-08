package com.wise.petadoption.security.integration;

import com.wise.petadoption.security.domain.SecurityUser;
import com.wise.petadoption.user.domain.User;
import com.wise.petadoption.user.exception.UserNotFoundException;
import com.wise.petadoption.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String email) {
        try {
            User user = userService.findByEmail(email);
            return SecurityUser.from(user);
        } catch (UserNotFoundException ex) {
            throw new UsernameNotFoundException(ex.getMessage(), ex);
        }
    }
}
