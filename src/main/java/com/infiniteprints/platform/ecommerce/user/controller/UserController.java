package com.infiniteprints.platform.ecommerce.user.controller;

import com.infiniteprints.platform.ecommerce.auth.entity.User;
import com.infiniteprints.platform.ecommerce.auth.repository.UserRepository;
import com.infiniteprints.platform.ecommerce.common.exception.ValidationException;
import com.infiniteprints.platform.ecommerce.user.dto.UpdateProfileRequest;
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

    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUser(
            Authentication authentication,
            @RequestBody UpdateProfileRequest request) {

        UUID userId = UUID.fromString(authentication.getName());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        validateProfile(request);

        user.setFirstName(clean(request.getFirstName()));
        user.setLastName(clean(request.getLastName()));
        user.setPhone(clean(request.getPhone()));
        user.setAddressLine1(clean(request.getAddressLine1()));
        user.setAddressCity(clean(request.getAddressCity()));
        user.setAddressState(clean(request.getAddressState()));
        user.setAddressPincode(clean(request.getAddressPincode()));

        User saved = userRepository.save(user);

        return ResponseEntity.ok(new UserResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getPhone(),
                saved.getAddressLine1(),
                saved.getAddressCity(),
                saved.getAddressState(),
                saved.getAddressPincode()
        ));
    }

    private void validateProfile(UpdateProfileRequest request) {
        if (isBlank(request.getFirstName())
                || isBlank(request.getLastName())
                || isBlank(request.getPhone())
                || isBlank(request.getAddressLine1())
                || isBlank(request.getAddressCity())
                || isBlank(request.getAddressState())
                || isBlank(request.getAddressPincode())) {
            throw new ValidationException("Name, phone, and full shipping address are required");
        }

        String phone = clean(request.getPhone());
        if (!phone.matches("^[0-9+\\-\\s()]{7,20}$")) {
            throw new ValidationException("Enter a valid phone number");
        }

        String pincode = clean(request.getAddressPincode());
        if (!pincode.matches("^[0-9]{6}$")) {
            throw new ValidationException("Enter a valid 6 digit pincode");
        }
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
