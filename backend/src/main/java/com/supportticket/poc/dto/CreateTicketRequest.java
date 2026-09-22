package com.supportticket.poc.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTicketRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String priority,
        @NotBlank String category
) {
}
