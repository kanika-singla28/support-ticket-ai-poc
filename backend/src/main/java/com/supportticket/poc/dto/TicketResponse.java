package com.supportticket.poc.dto;

import com.supportticket.poc.entity.SupportTicket;
import com.supportticket.poc.entity.SupportTicketComment;

import java.time.LocalDateTime;
import java.util.List;

public record TicketResponse(
        Long id,
        String title,
        String description,
        String priority,
        String status,
        String category,
        String assignee,
        String resolutionInformation,
        List<CommentResponse> comments,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static TicketResponse from(SupportTicket ticket) {

        List<CommentResponse> comments = ticket.getComments()
                .stream()
                .map(CommentResponse::from)
                .toList();

        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getCategory(),
                ticket.getAssignee(),
                ticket.getResolutionInformation(),
                comments,
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }

    public record CommentResponse(
            Long id,
            String content,
            LocalDateTime createdAt
    ) {
        static CommentResponse from(SupportTicketComment comment) {
            return new CommentResponse(
                    comment.getId(),
                    comment.getContent(),
                    comment.getCreatedAt()
            );
        }
    }
}
