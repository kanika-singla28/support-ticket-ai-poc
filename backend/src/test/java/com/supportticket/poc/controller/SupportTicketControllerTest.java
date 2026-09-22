package com.supportticket.poc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supportticket.poc.dto.CreateTicketRequest;
import com.supportticket.poc.exception.GlobalExceptionHandler;
import com.supportticket.poc.exception.InvalidStatusTransitionException;
import com.supportticket.poc.exception.TicketNotFoundException;
import com.supportticket.poc.service.SupportTicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SupportTicketControllerTest {

    private SupportTicketService supportTicketService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        supportTicketService = mock(SupportTicketService.class);

        SupportTicketController controller =
                new SupportTicketController(supportTicketService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldRejectInvalidCreateRequest() throws Exception {

        String request = """
                {
                  "title": "",
                  "description": "",
                  "priority": "CRITICAL",
                  "category": ""
                }
                """;

        mockMvc.perform(
                        post("/tickets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors").isArray());

        verifyNoInteractions(supportTicketService);
    }

    @Test
    void shouldRejectUnsupportedPriority() throws Exception {

        String request = """
                {
                  "title": "Checkout problem",
                  "description": "Customer cannot complete checkout.",
                  "priority": "CRITICAL",
                  "category": "PAYMENT"
                }
                """;

        mockMvc.perform(
                        post("/tickets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.errors[0]")
                                .value(
                                        "priority: priority must be LOW, MEDIUM, or HIGH"
                                )
                );

        verifyNoInteractions(supportTicketService);
    }

    @Test
    void shouldReturn404WhenTicketDoesNotExist() throws Exception {

        when(supportTicketService.getById(999L))
                .thenThrow(new TicketNotFoundException(999L));

        mockMvc.perform(
                        get("/tickets/999")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(
                        jsonPath("$.errors[0]")
                                .value("Support ticket not found: 999")
                );

        verify(supportTicketService).getById(999L);
    }

    @Test
    void shouldReturn409ForInvalidStatusTransition() throws Exception {

        when(
                supportTicketService.transitionStatus(
                        3L,
                        "CLOSED"
                )
        ).thenThrow(
                new InvalidStatusTransitionException(
                        "IN_PROGRESS",
                        "CLOSED"
                )
        );

        String request = """
                {
                  "status": "CLOSED"
                }
                """;

        mockMvc.perform(
                        patch("/tickets/3/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(
                        jsonPath("$.errors[0]")
                                .value(
                                        "Invalid ticket status transition: IN_PROGRESS -> CLOSED"
                                )
                );

        verify(supportTicketService)
                .transitionStatus(3L, "CLOSED");
    }

    @Test
    void shouldRejectInvalidStatusValueBeforeCallingService()
            throws Exception {

        String request = """
                {
                  "status": "REOPENED"
                }
                """;

        mockMvc.perform(
                        patch("/tickets/3/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());

        verifyNoInteractions(supportTicketService);
    }
}
