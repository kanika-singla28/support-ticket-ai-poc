package com.supportticket.poc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TransitionStatusRequest(

        @NotBlank
        @Pattern(
                regexp = "OPEN|IN_PROGRESS|RESOLVED|CLOSED|CANCELLED",
                message = "status must be OPEN, IN_PROGRESS, RESOLVED, CLOSED, or CANCELLED"
        )
        String status
) {
}
