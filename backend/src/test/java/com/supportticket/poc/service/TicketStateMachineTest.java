package com.supportticket.poc.service;

import com.supportticket.poc.exception.InvalidStatusTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketStateMachineTest {

    private TicketStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new TicketStateMachine();
    }

    @Test
    void shouldAllowOpenToInProgress() {
        assertDoesNotThrow(
                () -> stateMachine.validate("OPEN", "IN_PROGRESS")
        );
    }

    @Test
    void shouldAllowOpenToCancelled() {
        assertDoesNotThrow(
                () -> stateMachine.validate("OPEN", "CANCELLED")
        );
    }

    @Test
    void shouldAllowInProgressToResolved() {
        assertDoesNotThrow(
                () -> stateMachine.validate("IN_PROGRESS", "RESOLVED")
        );
    }

    @Test
    void shouldAllowInProgressToCancelled() {
        assertDoesNotThrow(
                () -> stateMachine.validate("IN_PROGRESS", "CANCELLED")
        );
    }

    @Test
    void shouldAllowResolvedToClosed() {
        assertDoesNotThrow(
                () -> stateMachine.validate("RESOLVED", "CLOSED")
        );
    }

    @Test
    void shouldRejectClosedToOpen() {
        assertThrows(
                InvalidStatusTransitionException.class,
                () -> stateMachine.validate("CLOSED", "OPEN")
        );
    }

    @Test
    void shouldRejectResolvedToOpen() {
        assertThrows(
                InvalidStatusTransitionException.class,
                () -> stateMachine.validate("RESOLVED", "OPEN")
        );
    }

    @Test
    void shouldRejectCancelledToOpen() {
        assertThrows(
                InvalidStatusTransitionException.class,
                () -> stateMachine.validate("CANCELLED", "OPEN")
        );
    }

    @Test
    void shouldRejectOutgoingTransitionFromClosed() {
        assertThrows(
                InvalidStatusTransitionException.class,
                () -> stateMachine.validate("CLOSED", "RESOLVED")
        );
    }

    @Test
    void shouldRejectOutgoingTransitionFromCancelled() {
        assertThrows(
                InvalidStatusTransitionException.class,
                () -> stateMachine.validate("CANCELLED", "IN_PROGRESS")
        );
    }

    @Test
    void shouldRejectUnknownCurrentStatus() {
        assertThrows(
                InvalidStatusTransitionException.class,
                () -> stateMachine.validate("UNKNOWN", "OPEN")
        );
    }
}
