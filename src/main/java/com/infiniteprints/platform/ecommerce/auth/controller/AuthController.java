package com.infiniteprints.platform.ecommerce.auth.controller;

import java.util.HashSet;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteprints.platform.ecommerce.auth.dto.AuthResponse;
import com.infiniteprints.platform.ecommerce.auth.dto.LoginRequest;
import com.infiniteprints.platform.ecommerce.auth.dto.RegisterRequest;
import com.infiniteprints.platform.ecommerce.auth.entity.User;
import com.infiniteprints.platform.ecommerce.auth.repository.UserRepository;
import com.infiniteprints.platform.ecommerce.auth.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already in use");
        }

        User u = new User();
        u.setEmail(request.getEmail());
        u.setPassword(passwordEncoder.encode(request.getPassword()));
        u.setFirstName(request.getFirstName());
        u.setLastName(request.getLastName());
        u.setRoles(new HashSet<>());
        u.getRoles().add("ROLE_USER");

        userRepository.save(u);

        return ResponseEntity.ok("registered");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        var auth = new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
        );

        authenticationManager.authenticate(auth);

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        var authorities = user.getRoles().stream()
                .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority(role))
                .toList();

        var userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );

        String token = tokenProvider.generateToken(userDetails);

        // Create response with token and user data
        var response = new AuthResponse(token);
        response.setUser(user);  // Include user object
        
        return ResponseEntity.ok(response);
    }
}
