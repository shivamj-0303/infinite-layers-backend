package com.infiniteprints.platform.ecommerce.config;

import java.util.Collections;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Simple in-memory UserDetailsService for development only.
 */
@Service
@Profile("dev")
public class DevUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if ("test@example.com".equalsIgnoreCase(username)) {
            // password: password (BCrypt) - replace in real dev with a hashed password
            return User.withUsername("test@example.com")
                    .password("$2a$10$TEST_HASH_PLACEHOLDER")
                    .authorities(Collections.emptyList())
                    .build();
        }
        throw new UsernameNotFoundException("User not found: " + username);
    }
}
