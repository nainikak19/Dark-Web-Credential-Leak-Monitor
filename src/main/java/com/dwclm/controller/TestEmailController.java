package com.dwclm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dwclm.service.EmailService;

@RestController
@RequestMapping("/api/test")
public class TestEmailController {

    @Autowired
    private EmailService emailService;

    // Sample endpoint to test sending breach alert email
    @GetMapping("/email")
    public ResponseEntity<String> testEmail(@RequestParam String to) {
        // Replace this with actual breaches if needed
        String breachedSites = "ExampleBreachSite.com, AnotherBreach.net";

        // Call the EmailService to send email
        emailService.sendBreachAlert(to, breachedSites);

        return ResponseEntity.ok("Email sent successfully to: " + to);
    }
}
