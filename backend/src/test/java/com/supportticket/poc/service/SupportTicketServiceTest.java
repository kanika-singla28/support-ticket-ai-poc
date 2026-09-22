package com.supportticket.poc.service;

import com.supportticket.poc.dto.AddCommentRequest;
import com.supportticket.poc.dto.UpdateTicketRequest;
import com.supportticket.poc.entity.SupportTicket;
import com.supportticket.poc.repository.SupportTicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportTicketServiceTest {

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
        ticket.setDescription(
                "Customer receives a timeout while paying with a credit card."
        );
        ticket.setPriority("HIGH");
        ticket.setCategory("PAYMENT");
        ticket.setStatus("OPEN");
    }

    @Test
    void shouldUpdateTicketAndRefreshKnowledge() {

        when(repository.findById(3L))
                .thenReturn(Optional.of(ticket));

        when(repository.saveAndFlush(any(SupportTicket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateTicketRequest request = new UpdateTicketRequest(
                "Checkout payment timeout - investigated",
                null,
                null,
                "Kanika",
                "Payment gateway retry configuration was corrected."
        );

        service.update(3L, request);

        assertEquals(
                "Checkout payment timeout - investigated",
                ticket.getTitle()
        );

        assertEquals("Kanika", ticket.getAssignee());

        assertEquals(
                "Payment gateway retry configuration was corrected.",
                ticket.getResolutionInformation()
        );

        verify(repository).saveAndFlush(ticket);
        verify(ticketKnowledgeService).refresh(ticket);
    }

    @Test
    void shouldAddCommentAndRefreshKnowledge() {

        when(repository.findById(3L))
                .thenReturn(Optional.of(ticket));

        when(repository.saveAndFlush(any(SupportTicket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AddCommentRequest request =
                new AddCommentRequest(
                        "Customer confirmed checkout is working."
                );

        service.addComment(3L, request);

        assertEquals(1, ticket.getComments().size());

        assertEquals(
                "Customer confirmed checkout is working.",
                ticket.getComments().get(0).getContent()
        );

        verify(repository).saveAndFlush(ticket);
        verify(ticketKnowledgeService).refresh(ticket);
    }

    @Test
    void shouldTransitionStatusAndRefreshKnowledge() {

        ticket.setStatus("OPEN");

        when(repository.findById(3L))
                .thenReturn(Optional.of(ticket));

        when(repository.saveAndFlush(any(SupportTicket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.transitionStatus(
                3L,
                "IN_PROGRESS"
        );

        verify(ticketStateMachine)
                .validate("OPEN", "IN_PROGRESS");

        assertEquals(
                "IN_PROGRESS",
                ticket.getStatus()
        );

        verify(repository).saveAndFlush(ticket);
        verify(ticketKnowledgeService).refresh(ticket);
    }

    @Test
    void shouldNotPersistWhenStatusTransitionIsRejected() {

        ticket.setStatus("IN_PROGRESS");

        when(repository.findById(3L))
                .thenReturn(Optional.of(ticket));

        doThrow(new RuntimeException("Invalid transition"))
                .when(ticketStateMachine)
                .validate("IN_PROGRESS", "CLOSED");

        assertThrows(
                RuntimeException.class,
                () -> service.transitionStatus(
                        3L,
                        "CLOSED"
                )
        );

        assertEquals(
                "IN_PROGRESS",
                ticket.getStatus()
        );

        verify(repository, never())
                .saveAndFlush(any());

        verify(ticketKnowledgeService, never())
                .refresh(any());
    }
}
