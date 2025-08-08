package com.dwclm.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class ManualBreachRequest {

    @NotBlank(message = "User email is required")
    @Email(message = "Invalid email format")
    private String userEmail;

    @NotBlank(message = "Breach name is required")
    private String breachName;

    @NotBlank(message = "Domain is required")
    private String domain;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Breach date is required")
    private LocalDate breachDate;
}
