package com.infiniteprints.platform.ecommerce.auth.controller;

import java.util.HashSet;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.infiniteprints.platform.ecommerce.auth.dto.AdminCreateRequest;
import com.infiniteprints.platform.ecommerce.auth.dto.AdminCreateResponse;
import com.infiniteprints.platform.ecommerce.auth.dto.PromoteUserRequest;
import com.infiniteprints.platform.ecommerce.auth.entity.User;
import com.infiniteprints.platform.ecommerce.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/setup")
@RequiredArgsConstructor
public class AdminSetupController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Setup endpoint to create initial admin user
     * This endpoint should only be accessible once (or in development)
     * In production, remove or protect this endpoint properly
     */
    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdmin(@RequestBody AdminCreateRequest request) {
        // Check if admin already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Admin user already exists");
        }

        // Create new admin user
        User admin = new User();
        admin.setEmail(request.getEmail());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setFirstName(request.getFirstName());
        admin.setLastName(request.getLastName());
        
        // Set roles with both USER and ADMIN
        var roles = new HashSet<String>();
        roles.add("ROLE_USER");
        roles.add("ROLE_ADMIN");
        admin.setRoles(roles);

        userRepository.save(admin);

        return ResponseEntity.ok(new AdminCreateResponse(
            admin.getId(),
            admin.getEmail(),
            admin.getFirstName(),
            admin.getLastName(),
            "Admin user created successfully"
        ));
    }

    /**
     * Add ADMIN role to existing user
     * Used to promote a regular user to admin
     */
    @PostMapping("/promote-user")
    public ResponseEntity<?> promoteUserToAdmin(@RequestBody PromoteUserRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        if (user.getRoles().contains("ROLE_ADMIN")) {
            return ResponseEntity.badRequest().body("User is already an admin");
        }

        user.getRoles().add("ROLE_ADMIN");
        userRepository.save(user);

        return ResponseEntity.ok("User promoted to admin successfully");
    }

    /**
     * Remove ADMIN role from user
     */
    @PostMapping("/demote-user")
    public ResponseEntity<?> demoteUserFromAdmin(@RequestBody PromoteUserRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        if (!user.getRoles().contains("ROLE_ADMIN")) {
            return ResponseEntity.badRequest().body("User is not an admin");
        }

        user.getRoles().remove("ROLE_ADMIN");
        userRepository.save(user);

        return ResponseEntity.ok("User demoted from admin successfully");
    }
}
