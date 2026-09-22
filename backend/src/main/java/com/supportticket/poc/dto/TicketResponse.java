package com.supportticket.poc.dto;

import com.supportticket.poc.entity.SupportTicket;

import java.time.LocalDateTime;

public record TicketResponse(
        Long id,
        String title,
        String description,
        String priority,
        String status,
        String category,
        LocalDateTime createdAt
) {
    public static TicketResponse from(SupportTicket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getCategory(),
                ticket.getCreatedAt()
        );
    }
}
