package com.supportticket.poc.service;

import com.supportticket.poc.entity.SupportTicket;
import com.supportticket.poc.entity.SupportTicketComment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketKnowledgeServiceTest {

    @Mock
    private PgVectorStore pgVectorStore;

    private TicketKnowledgeService service;
    private SupportTicket ticket;

    @BeforeEach
    void setUp() {
        service = new TicketKnowledgeService(pgVectorStore);

        ticket = new SupportTicket();
        ReflectionTestUtils.setField(ticket, "id", 3L);
        ticket.setTitle("Checkout payment timeout");
        ticket.setDescription(
                "Customer receives a timeout while paying with a credit card."
        );
        ticket.setPriority("HIGH");
        ticket.setStatus("IN_PROGRESS");
        ticket.setCategory("PAYMENT");
        ticket.setAssignee("Kanika Singh");
        ticket.setResolutionInformation(
                "Payment gateway retry configuration was corrected."
        );
    }

    @Test
    void shouldDeleteOldDocumentBeforeAddingRefreshedDocument() {

        service.refresh(ticket);

        ArgumentCaptor<List<String>> deleteCaptor =
                ArgumentCaptor.forClass(List.class);

        ArgumentCaptor<List<Document>> addCaptor =
                ArgumentCaptor.forClass(List.class);

        var ordered = inOrder(pgVectorStore);

        ordered.verify(pgVectorStore)
                .delete(deleteCaptor.capture());

        ordered.verify(pgVectorStore)
                .add(addCaptor.capture());

        assertEquals(1, deleteCaptor.getValue().size());
        assertEquals(1, addCaptor.getValue().size());

        assertEquals(
                deleteCaptor.getValue().get(0),
                addCaptor.getValue().get(0).getId()
        );
    }

    @Test
    void shouldUseStableDocumentIdForSameTicket() {

        service.refresh(ticket);
        service.refresh(ticket);

        ArgumentCaptor<List<Document>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(pgVectorStore, times(2))
                .add(captor.capture());

        String firstId =
                captor.getAllValues().get(0).get(0).getId();

        String secondId =
                captor.getAllValues().get(1).get(0).getId();

        assertEquals(firstId, secondId);
    }

    @Test
    void shouldIndexTicketFieldsResolutionAndComments() {

        SupportTicketComment comment =
                new SupportTicketComment();

        comment.setContent(
                "Customer confirmed checkout works."
        );

        ticket.getComments().add(comment);

        service.refresh(ticket);

        ArgumentCaptor<List<Document>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(pgVectorStore).add(captor.capture());

        Document document =
                captor.getValue().get(0);

        String content = document.getText();

        assertTrue(content.contains("Ticket ID: 3"));
        assertTrue(content.contains(
                "Title: Checkout payment timeout"
        ));
        assertTrue(content.contains("Priority: HIGH"));
        assertTrue(content.contains("Status: IN_PROGRESS"));
        assertTrue(content.contains("Category: PAYMENT"));
        assertTrue(content.contains(
                "Assignee: Kanika Singh"
        ));
        assertTrue(content.contains(
                "Payment gateway retry configuration was corrected."
        ));
        assertTrue(content.contains(
                "- Customer confirmed checkout works."
        ));
    }

    @Test
    void shouldAddExpectedMetadata() {

        service.refresh(ticket);

        ArgumentCaptor<List<Document>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(pgVectorStore).add(captor.capture());

        Document document =
                captor.getValue().get(0);

        assertEquals(
                "3",
                document.getMetadata().get("ticketId")
        );
        assertEquals(
                "Checkout payment timeout",
                document.getMetadata().get("title")
        );
        assertEquals(
                "HIGH",
                document.getMetadata().get("priority")
        );
        assertEquals(
                "IN_PROGRESS",
                document.getMetadata().get("status")
        );
        assertEquals(
                "PAYMENT",
                document.getMetadata().get("category")
        );
        assertEquals(
                "Kanika Singh",
                document.getMetadata().get("assignee")
        );
    }

    @Test
    void shouldHandleMissingOptionalKnowledgeFields() {

        ticket.setAssignee(null);
        ticket.setResolutionInformation(null);

        service.refresh(ticket);

        ArgumentCaptor<List<Document>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(pgVectorStore).add(captor.capture());

        Document document =
                captor.getValue().get(0);

        assertTrue(
                document.getText().contains(
                        "Assignee: Unassigned"
                )
        );
        assertTrue(
                document.getText().contains(
                        "Not provided"
                )
        );
        assertTrue(
                document.getText().contains(
                        "Comments:\nNone"
                )
        );

        assertFalse(
                document.getMetadata()
                        .containsKey("assignee")
        );
    }
}
