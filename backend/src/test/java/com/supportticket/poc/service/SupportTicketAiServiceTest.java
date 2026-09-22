package com.supportticket.poc.service;

import com.supportticket.poc.dto.AskAiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportTicketAiServiceTest {

    @Mock
    private PgVectorStore pgVectorStore;

    @Mock
    private ChatModel chatModel;

    private SupportTicketAiService service;

    @BeforeEach
    void setUp() {
        service = new SupportTicketAiService(
                pgVectorStore,
                chatModel,
                5,
                0.75
        );
    }

    @Test
    void shouldReturnNoRelevantTicketWhenSearchReturnsEmptyList() {

        when(pgVectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of());

        AskAiResponse response =
                service.ask("What caused the checkout problem?");

        assertFalse(response.relevantTicketsFound());
        assertEquals(
                "I could not find a relevant support ticket for this question.",
                response.answer()
        );
        assertTrue(response.sources().isEmpty());
    }

    @Test
    void shouldReturnNoRelevantTicketWhenSearchReturnsNull() {

        when(pgVectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(null);

        AskAiResponse response =
                service.ask("Unknown support issue");

        assertFalse(response.relevantTicketsFound());
        assertTrue(response.sources().isEmpty());
    }

    @Test
    void shouldBuildSearchRequestUsingConfiguredRagSettings() {

        when(pgVectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of());

        service.ask("checkout timeout");

        ArgumentCaptor<SearchRequest> captor =
                ArgumentCaptor.forClass(SearchRequest.class);

        verify(pgVectorStore)
                .similaritySearch(captor.capture());

        SearchRequest request = captor.getValue();

        assertEquals("checkout timeout", request.getQuery());
        assertEquals(5, request.getTopK());
        assertEquals(
                0.75,
                request.getSimilarityThreshold(),
                0.000001
        );
    }

    @Test
    void shouldPerformOnlyOneVectorSearchPerQuestion() {

        when(pgVectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of());

        service.ask("checkout timeout");

        verify(pgVectorStore, times(1))
                .similaritySearch(any(SearchRequest.class));
    }

    @Test
    void shouldNotCallChatModelWhenNoRelevantTicketExists() {

        when(pgVectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of());

        clearInvocations(chatModel);

        service.ask("completely unrelated question");

        verifyNoInteractions(chatModel);
    }
}
