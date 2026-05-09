package com.infiniteprints.platform.ecommerce.user.controller;

import com.infiniteprints.platform.ecommerce.auth.entity.User;
import com.infiniteprints.platform.ecommerce.auth.repository.UserRepository;
import com.infiniteprints.platform.ecommerce.user.dto.UserResponse;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserResponse response = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getAddressLine1(),
                user.getAddressCity(),
                user.getAddressState(),
                user.getAddressPincode()
        );

        return ResponseEntity.ok(response);
    }
}