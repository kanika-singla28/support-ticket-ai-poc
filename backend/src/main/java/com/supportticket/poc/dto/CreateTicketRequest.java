package com.supportticket.poc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(

        @NotBlank
        @Size(max = 200)
        String title,

        @NotBlank
        @Size(max = 5000)
        String description,

        @NotBlank
        @Pattern(
                regexp = "LOW|MEDIUM|HIGH",
                message = "priority must be LOW, MEDIUM, or HIGH"
        )
        String priority,

        @NotBlank
        @Size(max = 100)
        String category
) {
}
