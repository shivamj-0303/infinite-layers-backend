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

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashSet;

import com.infiniteprints.platform.ecommerce.auth.dto.SendRegistrationOtpRequest;
import com.infiniteprints.platform.ecommerce.auth.dto.VerifyRegistrationOtpRequest;
import com.infiniteprints.platform.ecommerce.auth.entity.RegistrationOtp;
import com.infiniteprints.platform.ecommerce.auth.repository.RegistrationOtpRepository;
import com.infiniteprints.platform.ecommerce.auth.service.GoogleMailService;

import com.infiniteprints.platform.ecommerce.auth.dto.AuthResponse;
import com.infiniteprints.platform.ecommerce.auth.dto.LoginRequest;
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
    private final RegistrationOtpRepository registrationOtpRepository;

    private final GoogleMailService googleMailService;

    private static final SecureRandom secureRandom =
            new SecureRandom();
    @PostMapping("/send-registration-otp")
    public ResponseEntity<?> sendRegistrationOtp(
            @RequestBody SendRegistrationOtpRequest request
    ) {

        if (userRepository.existsByEmail(request.getEmail())) {

            return ResponseEntity
                    .badRequest()
                    .body("Email already exists");
        }

        String otp = String.format(
                "%06d",
                secureRandom.nextInt(1000000)
        );

        registrationOtpRepository.deleteByEmail(
                request.getEmail()
        );

        RegistrationOtp registrationOtp = new RegistrationOtp();

        registrationOtp.setEmail(request.getEmail());

        registrationOtp.setOtp(otp);

        registrationOtp.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        registrationOtp.setFirstName(request.getFirstName());

        registrationOtp.setLastName(request.getLastName());

        registrationOtp.setPhone(request.getPhone());

        registrationOtp.setExpiresAt(
                LocalDateTime.now().plusMinutes(5)
        );

        registrationOtpRepository.save(registrationOtp);

        googleMailService.sendOtpEmail(
                request.getEmail(),
                otp
        );

        return ResponseEntity.ok(
                "OTP sent successfully"
        );
    }

    @PostMapping("/verify-registration-otp")
    public ResponseEntity<?> verifyRegistrationOtp(
            @RequestBody VerifyRegistrationOtpRequest request
    ) {

        RegistrationOtp registrationOtp =
                registrationOtpRepository
                        .findByEmail(request.getEmail())
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "OTP not found"
                                )
                        );

        if (
                registrationOtp
                        .getExpiresAt()
                        .isBefore(LocalDateTime.now())
        ) {

            registrationOtpRepository.delete(registrationOtp);

            return ResponseEntity
                    .badRequest()
                    .body("OTP expired");
        }

        if (
                !registrationOtp
                        .getOtp()
                        .equals(request.getOtp())
        ) {

            return ResponseEntity
                    .badRequest()
                    .body("Invalid OTP");
        }

        User user = new User();

        user.setEmail(registrationOtp.getEmail());

        user.setPassword(registrationOtp.getPassword());

        user.setFirstName(registrationOtp.getFirstName());

        user.setLastName(registrationOtp.getLastName());

        user.setPhone(registrationOtp.getPhone());

        user.setRoles(new HashSet<>());

        user.getRoles().add("ROLE_USER");

        userRepository.save(user);

        registrationOtpRepository.delete(registrationOtp);

        String token = tokenProvider.generateToken(user);

        return ResponseEntity.ok(
                new AuthResponse(token)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = tokenProvider.generateToken(user);

        return ResponseEntity.ok(new AuthResponse(token));
    }
}
