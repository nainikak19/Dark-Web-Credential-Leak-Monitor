package com.dwclm.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class BreachResponseDTO {
    private Long id;
    private String breachName;
    private String domain;
    private LocalDate breachDate;
    private String description;

    // Basic user info
    private String userEmail;
    private String userFullName;
}
