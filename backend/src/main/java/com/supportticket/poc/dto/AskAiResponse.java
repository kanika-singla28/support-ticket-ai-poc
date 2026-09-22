package com.supportticket.poc.dto;

import java.util.List;

public record AskAiResponse(
        String answer,
        boolean relevantTicketsFound,
        List<Source> sources
) {
    public record Source(Long ticketId) {
    }
}
