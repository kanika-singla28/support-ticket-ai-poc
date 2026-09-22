package com.supportticket.poc.service;

import com.supportticket.poc.dto.AddCommentRequest;
import com.supportticket.poc.dto.CreateTicketRequest;
import com.supportticket.poc.dto.TicketResponse;
import com.supportticket.poc.dto.UpdateTicketRequest;
import com.supportticket.poc.entity.SupportTicket;
import com.supportticket.poc.entity.SupportTicketComment;
import com.supportticket.poc.exception.InvalidTicketRequestException;
import com.supportticket.poc.exception.TicketNotFoundException;
import com.supportticket.poc.repository.SupportTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupportTicketService {

    private final SupportTicketRepository repository;
    private final TicketKnowledgeService ticketKnowledgeService;
    private final TicketStateMachine ticketStateMachine;

    public SupportTicketService(
            SupportTicketRepository repository,
            TicketKnowledgeService ticketKnowledgeService,
            TicketStateMachine ticketStateMachine) {
        this.repository = repository;
        this.ticketKnowledgeService = ticketKnowledgeService;
        this.ticketStateMachine = ticketStateMachine;
    }

    @Transactional
    public TicketResponse create(CreateTicketRequest request) {

        SupportTicket ticket = new SupportTicket();
        ticket.setTitle(request.title().trim());
        ticket.setDescription(request.description().trim());
        ticket.setPriority(request.priority());
        ticket.setCategory(request.category().trim());
        ticket.setStatus("OPEN");

        SupportTicket saved = repository.saveAndFlush(ticket);

        ticketKnowledgeService.refresh(saved);

        return TicketResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> list(String search, String status) {

        String normalizedSearch = normalize(search);
        String normalizedStatus = normalize(status);

        List<SupportTicket> tickets;

        if (normalizedSearch == null && normalizedStatus == null) {
            tickets = repository.findAllByOrderByCreatedAtDesc();

        } else if (normalizedSearch == null) {
            tickets = repository.findByStatusOrderByCreatedAtDesc(
                    normalizedStatus
            );

        } else if (normalizedStatus == null) {
            tickets =
                    repository
                            .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByCreatedAtDesc(
                                    normalizedSearch,
                                    normalizedSearch
                            );

        } else {
            tickets =
                    repository
                            .findByStatusAndTitleContainingIgnoreCaseOrStatusAndDescriptionContainingIgnoreCaseOrderByCreatedAtDesc(
                                    normalizedStatus,
                                    normalizedSearch,
                                    normalizedStatus,
                                    normalizedSearch
                            );
        }

        return tickets.stream()
                .map(TicketResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketResponse getById(Long ticketId) {
        return TicketResponse.from(findTicket(ticketId));
    }

    @Transactional
    public TicketResponse update(
            Long ticketId,
            UpdateTicketRequest request) {

        SupportTicket ticket = findTicket(ticketId);
        boolean changed = false;

        if (request.title() != null) {
            ticket.setTitle(requireNonBlank(request.title(), "title"));
            changed = true;
        }

        if (request.description() != null) {
            ticket.setDescription(
                    requireNonBlank(request.description(), "description")
            );
            changed = true;
        }

        if (request.priority() != null) {
            ticket.setPriority(request.priority());
            changed = true;
        }

        if (request.assignee() != null) {
            ticket.setAssignee(normalizeNullableValue(request.assignee()));
            changed = true;
        }

        if (request.resolutionInformation() != null) {
            ticket.setResolutionInformation(
                    normalizeNullableValue(request.resolutionInformation())
            );
            changed = true;
        }

        if (!changed) {
            throw new InvalidTicketRequestException(
                    "At least one editable ticket field must be supplied."
            );
        }

        SupportTicket saved = repository.saveAndFlush(ticket);

        ticketKnowledgeService.refresh(saved);

        return TicketResponse.from(saved);
    }

    @Transactional
    public TicketResponse addComment(
            Long ticketId,
            AddCommentRequest request) {

        SupportTicket ticket = findTicket(ticketId);

        SupportTicketComment comment = new SupportTicketComment();
        comment.setTicket(ticket);
        comment.setContent(request.content().trim());

        ticket.getComments().add(comment);

        SupportTicket saved = repository.saveAndFlush(ticket);

        ticketKnowledgeService.refresh(saved);

        return TicketResponse.from(saved);
    }

    @Transactional
    public TicketResponse transitionStatus(
            Long ticketId,
            String requestedStatus) {

        SupportTicket ticket = findTicket(ticketId);

        ticketStateMachine.validate(
                ticket.getStatus(),
                requestedStatus
        );

        ticket.setStatus(requestedStatus);

        SupportTicket saved = repository.saveAndFlush(ticket);

        ticketKnowledgeService.refresh(saved);

        return TicketResponse.from(saved);
    }

    private SupportTicket findTicket(Long ticketId) {
        return repository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
    }

    private String requireNonBlank(String value, String fieldName) {

        String normalized = normalize(value);

        if (normalized == null) {
            throw new InvalidTicketRequestException(
                    fieldName + " must not be blank."
            );
        }

        return normalized;
    }

    private String normalizeNullableValue(String value) {
        return normalize(value);
    }

    private String normalize(String value) {

        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty() ? null : normalized;
    }
}
