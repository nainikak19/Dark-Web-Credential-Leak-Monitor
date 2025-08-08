package com.dwclm.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dwclm.model.BreachResult;
import com.dwclm.repository.BreachResultRepository;
import com.dwclm.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminStatsController {

    private final UserRepository userRepository;
    private final BreachResultRepository breachResultRepository;

    // 1. Total number of users
    @GetMapping("/users/count")
    public long getUserCount() {
        return userRepository.count();
    }

    // 2. Total number of breached users
    @GetMapping("/users/breached/count")
    public long getBreachedUserCount() {
        return userRepository.countByBreachedTrue();
    }

    // 3. List of recently breached users (last 7 days)
    @GetMapping("/users/breached/recent")
    public List<BreachResult> getRecentBreaches() {
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        return breachResultRepository.findByBreachDateAfter(sevenDaysAgo);
    }

    // 4. Breaches on a specific day
    @GetMapping("/breaches/date")
    public List<BreachResult> getBreachesByDate(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return breachResultRepository.findByBreachDate(date);
    }

    // 5. Top 5 most frequently breached emails
    @GetMapping("/breaches/top")
    public List<Map<String, Object>> getTopBreachedEmails() {
        List<Object[]> results = breachResultRepository.findTopBreachedEmails();
        List<Map<String, Object>> response = results.stream().map(obj -> Map.of(
                "email", obj[0],
                "breachCount", obj[1]
        )).toList();
        return response;
    }
}
