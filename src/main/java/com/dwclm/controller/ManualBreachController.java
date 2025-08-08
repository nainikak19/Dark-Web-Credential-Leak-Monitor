package com.dwclm.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dwclm.dto.ManualBreachRequest;
import com.dwclm.model.BreachResult;
import com.dwclm.model.User;
import com.dwclm.repository.BreachResultRepository;
import com.dwclm.repository.UserRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin/manual-breach")
public class ManualBreachController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BreachResultRepository breachResultRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addManualBreach(@Valid @RequestBody ManualBreachRequest request) {
        // 1. Check if user exists
        User user = userRepository.findByEmail(request.getUserEmail()).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found with email: " + request.getUserEmail()));
        }

        // 2. Check if breach already exists for this user and breach name
        boolean alreadyExists = breachResultRepository.existsByUserIdAndBreachName(user.getId(), request.getBreachName());
        if (alreadyExists) {
            return ResponseEntity.badRequest().body(Map.of("error", "This breach already exists for the user."));
        }

        // 3. Create and save breach result
        BreachResult result = BreachResult.builder()
                .user(user)
                .breachName(request.getBreachName())
                .domain(request.getDomain())
                .description(request.getDescription())
                .breachDate(request.getBreachDate())
                .detectedAt(LocalDateTime.now())
                .build();

        breachResultRepository.save(result);

        // 4. Update user breach status and save
        user.setBreached(true);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Manual breach entry added successfully."));
    }
}
