package com.dwclm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ManualBreachDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Breach name is required")
    private String breachName;

    @NotNull(message = "Breach date is required")
    private LocalDate breachDate;

    @NotBlank(message = "Domain is required")
    private String domain;

    @NotBlank(message = "Description is required")
    private String description;
}
