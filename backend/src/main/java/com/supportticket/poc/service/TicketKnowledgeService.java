package com.supportticket.poc.service;

import com.supportticket.poc.entity.SupportTicket;
import com.supportticket.poc.entity.SupportTicketComment;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Service
public class TicketKnowledgeService {

    private final PgVectorStore pgVectorStore;

    public TicketKnowledgeService(PgVectorStore pgVectorStore) {
        this.pgVectorStore = pgVectorStore;
    }

    public void refresh(SupportTicket ticket) {

        String documentId = documentId(ticket.getId());

        // Remove the previous representation so stale ticket knowledge
        // cannot remain searchable after an update.
        pgVectorStore.delete(List.of(documentId));

        Document document = buildDocument(ticket);

        pgVectorStore.add(List.of(document));
    }

    private Document buildDocument(SupportTicket ticket) {

        String comments = ticket.getComments()
                .stream()
                .map(SupportTicketComment::getContent)
                .map(content -> "- " + content)
                .collect(Collectors.joining("\n"));

        if (comments.isBlank()) {
            comments = "None";
        }

        String assignee = ticket.getAssignee() == null
                ? "Unassigned"
                : ticket.getAssignee();

        String resolution = ticket.getResolutionInformation() == null
                ? "Not provided"
                : ticket.getResolutionInformation();

        String content = """
                Support Ticket
                Ticket ID: %d
                Title: %s
                Description: %s
                Priority: %s
                Status: %s
                Category: %s
                Assignee: %s

                Resolution:
                %s

                Comments:
                %s
                """.formatted(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getCategory(),
                assignee,
                resolution,
                comments
        );

        Map<String, Object> metadata = new HashMap<>();

        metadata.put("ticketId", ticket.getId().toString());
        metadata.put("title", ticket.getTitle());
        metadata.put("priority", ticket.getPriority());
        metadata.put("status", ticket.getStatus());
        metadata.put("category", ticket.getCategory());

        if (ticket.getAssignee() != null) {
            metadata.put("assignee", ticket.getAssignee());
        }

        return new Document(
                documentId(ticket.getId()),
                content,
                metadata
        );
    }

    private String documentId(Long ticketId) {
        String stableKey = "support-ticket:" + ticketId;

        return UUID.nameUUIDFromBytes(
                stableKey.getBytes(StandardCharsets.UTF_8)
        ).toString();
    }
}
