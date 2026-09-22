package com.supportticket.poc.service;

import com.supportticket.poc.entity.SupportTicket;
import com.supportticket.poc.repository.SupportTicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportTicketListServiceTest {

    @Mock
    private SupportTicketRepository repository;

    @Mock
    private TicketKnowledgeService ticketKnowledgeService;

    @Mock
    private TicketStateMachine ticketStateMachine;

    @InjectMocks
    private SupportTicketService service;

    private SupportTicket ticket;

    @BeforeEach
    void setUp() {
        ticket = new SupportTicket();
        ticket.setTitle("Checkout payment timeout");
        ticket.setDescription("Customer cannot complete checkout.");
        ticket.setPriority("HIGH");
        ticket.setCategory("PAYMENT");
        ticket.setStatus("IN_PROGRESS");
    }

    @Test
    void shouldListAllTicketsWhenNoFiltersProvided() {

        when(repository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(ticket));

        var result = service.list(null, null);

        assertEquals(1, result.size());

        verify(repository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void shouldFilterTicketsByStatus() {

        when(repository.findByStatusOrderByCreatedAtDesc("IN_PROGRESS"))
                .thenReturn(List.of(ticket));

        var result = service.list(null, "IN_PROGRESS");

        assertEquals(1, result.size());

        verify(repository)
                .findByStatusOrderByCreatedAtDesc("IN_PROGRESS");
    }

    @Test
    void shouldSearchTicketsByKeyword() {

        when(repository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByCreatedAtDesc(
                        "checkout",
                        "checkout"
                ))
                .thenReturn(List.of(ticket));

        var result = service.list("checkout", null);

        assertEquals(1, result.size());

        verify(repository)
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByCreatedAtDesc(
                        "checkout",
                        "checkout"
                );
    }

    @Test
    void shouldSearchAndFilterTicketsTogether() {

        when(repository
                .findByStatusAndTitleContainingIgnoreCaseOrStatusAndDescriptionContainingIgnoreCaseOrderByCreatedAtDesc(
                        "IN_PROGRESS",
                        "checkout",
                        "IN_PROGRESS",
                        "checkout"
                ))
                .thenReturn(List.of(ticket));

        var result = service.list(
                "checkout",
                "IN_PROGRESS"
        );

        assertEquals(1, result.size());

        verify(repository)
                .findByStatusAndTitleContainingIgnoreCaseOrStatusAndDescriptionContainingIgnoreCaseOrderByCreatedAtDesc(
                        "IN_PROGRESS",
                        "checkout",
                        "IN_PROGRESS",
                        "checkout"
                );
    }

    @Test
    void shouldTreatBlankSearchAndStatusAsNoFilters() {

        when(repository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(ticket));

        var result = service.list("   ", "   ");

        assertEquals(1, result.size());

        verify(repository).findAllByOrderByCreatedAtDesc();
    }
}
