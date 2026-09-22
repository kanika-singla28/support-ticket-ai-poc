package com.supportticket.poc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddCommentRequest(

        @NotBlank
        @Size(max = 5000)
        String content
) {
}
