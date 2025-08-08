package com.dwclm.controller;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dwclm.dto.BreachResponseDTO;
import com.dwclm.dto.DashboardMetricsDTO;
import com.dwclm.dto.ManualBreachDTO;
import com.dwclm.model.BreachResult;
import com.dwclm.model.User;
import com.dwclm.repository.BreachResultRepository;
import com.dwclm.repository.UserRepository;
import com.dwclm.service.BreachScannerService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/breach")
@RequiredArgsConstructor
public class BreachScannerController {

    private final BreachScannerService breachScannerService;
    private final UserRepository userRepository;
    private final BreachResultRepository breachResultRepository;

    // Trigger breach scan
    @GetMapping("/scan")
    public ResponseEntity<String> scanUsers(@RequestParam(required = false) String email) {
        if (email != null) {
            Optional<User> optionalUser = userRepository.findByEmail(email);
            if (optionalUser.isPresent()) {
                boolean breached = breachScannerService.scanSingleUser(optionalUser.get());
                return ResponseEntity.ok(breached ?
                        "Breach detected for " + email :
                        "No breach detected for " + email);
            } else {
                return ResponseEntity.badRequest().body("User with email " + email + " not found.");
            }
        }

        breachScannerService.scanAllUsers();
        return ResponseEntity.ok("Breach scan completed for all users!");
    }

    // Get all users
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // Get breaches for a user by ID or param, with clean DTO
    @GetMapping({"/user/{userId}/breaches", "/user/breaches"})
    public ResponseEntity<?> getBreachesForUser(
            @PathVariable(required = false) Long userId,
            @RequestParam(required = false) Long userIdParam) {

        Long finalUserId = (userId != null) ? userId : userIdParam;
        if (finalUserId == null) {
            return ResponseEntity.badRequest().body("User ID is required.");
        }

        List<BreachResult> breaches = breachResultRepository.findByUserId(finalUserId);
        List<BreachResponseDTO> dtoList = breaches.stream().map(this::mapToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    // Summary of breaches
    @GetMapping("/summary")
    public ResponseEntity<List<Map<String, Object>>> getBreachSummary() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> summaryList = new ArrayList<>();

        for (User user : users) {
            Map<String, Object> summary = new HashMap<>();
            summary.put("userId", user.getId());
            summary.put("email", user.getEmail());
            summary.put("isBreached", user.isBreached());

            List<BreachResult> breaches = breachResultRepository.findByUserId(user.getId());
            summary.put("totalBreaches", breaches.size());

            if (!breaches.isEmpty()) {
                LocalDate latestDate = breaches.stream()
                        .map(BreachResult::getBreachDate)
                        .max(LocalDate::compareTo)
                        .orElse(null);
                summary.put("lastBreachDate", latestDate);
            } else {
                summary.put("lastBreachDate", "N/A");
            }

            summaryList.add(summary);
        }

        return ResponseEntity.ok(summaryList);
    }

    // Breaches by email (with DTO)
    @GetMapping("/email/{email}/breaches")
    public ResponseEntity<?> getBreachesByEmail(@PathVariable String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found with email: " + email);
        }

        List<BreachResult> breaches = breachResultRepository.findByUserId(userOpt.get().getId());
        List<BreachResponseDTO> dtoList = breaches.stream().map(this::mapToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    // CSV export
    @GetMapping("/export/csv")
    public void exportFilteredBreachDataToCSV(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate breachDate,
            HttpServletResponse response) throws Exception {

        response.setContentType("text/csv");
        String fileName = "filtered_breach_data.csv";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(fileName, "UTF-8") + "\"");

        List<BreachResult> breaches;

        if (userId != null) {
            breaches = breachResultRepository.findByUserId(userId);
        } else if (email != null) {
            breaches = breachResultRepository.findByUserEmail(email);
        } else if (breachDate != null) {
            breaches = breachResultRepository.findByBreachDate(breachDate);
        } else {
            breaches = breachResultRepository.findAll();
        }

        try (PrintWriter writer = response.getWriter()) {
            writer.println("Email,Breach Name,Breach Date,Domain,Description");
            for (BreachResult breach : breaches) {
                String userEmail = breach.getUser() != null ? breach.getUser().getEmail() : "N/A";
                writer.printf("%s,%s,%s,%s,%s%n",
                        userEmail,
                        breach.getBreachName(),
                        breach.getBreachDate(),
                        breach.getDomain(),
                        breach.getDescription().replace(",", " "));
            }
        }
    }

    // Dashboard metrics
    @GetMapping("/metrics")
    public ResponseEntity<DashboardMetricsDTO> getMetrics() {
        return ResponseEntity.ok(breachScannerService.getDashboardMetrics());
    }

    // Manual breach add
    @PostMapping("/add")
    public ResponseEntity<String> addManualBreach(@RequestBody ManualBreachDTO dto) {
        Optional<User> optionalUser = userRepository.findByEmail(dto.getEmail());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("User with email " + dto.getEmail() + " not found.");
        }

        User user = optionalUser.get();
        BreachResult breach = new BreachResult();
        breach.setUser(user);
        breach.setBreachName(dto.getBreachName());
        breach.setBreachDate(dto.getBreachDate());
        breach.setDomain(dto.getDomain());
        breach.setDescription(dto.getDescription());

        breachResultRepository.save(breach);

        user.setBreached(true);
        userRepository.save(user);

        return ResponseEntity.ok("Breach added successfully for " + dto.getEmail());
    }

    // Utility method to map BreachResult to DTO
    private BreachResponseDTO mapToDTO(BreachResult breach) {
        BreachResponseDTO dto = new BreachResponseDTO();
        dto.setId(breach.getId());
        dto.setBreachName(breach.getBreachName());
        dto.setDomain(breach.getDomain());
        dto.setBreachDate(breach.getBreachDate());
        dto.setDescription(breach.getDescription());

        if (breach.getUser() != null) {
            dto.setUserEmail(breach.getUser().getEmail());
            dto.setUserFullName(breach.getUser().getFullName());
        }

        return dto;
    }
}
