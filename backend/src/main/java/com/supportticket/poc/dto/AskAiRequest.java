package com.supportticket.poc.dto;

import jakarta.validation.constraints.NotBlank;

public record AskAiRequest(
        @NotBlank String question
) {
}
