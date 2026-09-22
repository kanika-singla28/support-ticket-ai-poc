package com.supportticket.poc.service;

import com.supportticket.poc.dto.CreateTicketRequest;
import com.supportticket.poc.dto.TicketResponse;
import com.supportticket.poc.entity.SupportTicket;
import com.supportticket.poc.repository.SupportTicketRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class SupportTicketService {

    private final SupportTicketRepository repository;
    private final PgVectorStore pgVectorStore;

    public SupportTicketService(
            SupportTicketRepository repository,
            PgVectorStore pgVectorStore) {
        this.repository = repository;
        this.pgVectorStore = pgVectorStore;
    }

    @Transactional
    public TicketResponse create(CreateTicketRequest request) {

        SupportTicket ticket = new SupportTicket();
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setPriority(request.priority());
        ticket.setCategory(request.category());
        ticket.setStatus("OPEN");

        SupportTicket saved = repository.saveAndFlush(ticket);

        indexTicket(saved);

        return TicketResponse.from(saved);
    }

    private void indexTicket(SupportTicket ticket) {

        String content = """
                Support Ticket
                Ticket ID: %d
                Title: %s
                Description: %s
                Priority: %s
                Status: %s
                Category: %s
                """.formatted(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getCategory()
        );

        Document document = new Document(
                "ticket-" + ticket.getId(),
                content,
                Map.of(
                        "ticketId", ticket.getId().toString(),
                        "title", ticket.getTitle(),
                        "priority", ticket.getPriority(),
                        "status", ticket.getStatus(),
                        "category", ticket.getCategory()
                )
        );

        pgVectorStore.add(List.of(document));
    }
}
