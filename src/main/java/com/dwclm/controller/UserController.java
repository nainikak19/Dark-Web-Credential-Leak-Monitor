package com.dwclm.controller;

import java.util.Collections;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dwclm.dto.LoginRequest;
import com.dwclm.jwt.JwtTokenGenerator;
import com.dwclm.model.Role;
import com.dwclm.model.User;
import com.dwclm.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenGenerator jwtTokenGenerator;

    @PostMapping("/register")
    public User registerUser(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String role,
            @RequestBody(required = false) User userFromBody) {

        if (userFromBody != null) {
            // Encode password from body
            userFromBody.setPassword(passwordEncoder.encode(userFromBody.getPassword()));
            if (userFromBody.getRole() == null) {
                userFromBody.setRole(Role.USER);
            }
            userFromBody.setBreached(false);
            return userRepository.save(userFromBody);
        }

        // Via query params
        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role((role != null) ? Role.valueOf(role.toUpperCase()) : Role.USER)
                .breached(false)
                .build();

        return userRepository.save(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password");
        }

        String token = jwtTokenGenerator.generateToken(user);
        return ResponseEntity.ok(Collections.singletonMap("token", token));
    }
}
