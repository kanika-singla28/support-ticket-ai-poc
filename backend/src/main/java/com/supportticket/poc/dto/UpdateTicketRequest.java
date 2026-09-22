package com.supportticket.poc.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateTicketRequest(

        @Size(max = 200)
        String title,

        @Size(max = 5000)
        String description,

        @Pattern(
                regexp = "LOW|MEDIUM|HIGH",
                message = "priority must be LOW, MEDIUM, or HIGH"
        )
        String priority,

        @Size(max = 200)
        String assignee,

        @Size(max = 5000)
        String resolutionInformation
) {
}
