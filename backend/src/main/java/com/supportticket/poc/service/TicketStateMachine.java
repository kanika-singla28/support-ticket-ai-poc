package com.supportticket.poc.service;

import com.supportticket.poc.exception.InvalidStatusTransitionException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class TicketStateMachine {

    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = Map.of(
            "OPEN", Set.of("IN_PROGRESS", "CANCELLED"),
            "IN_PROGRESS", Set.of("RESOLVED", "CANCELLED"),
            "RESOLVED", Set.of("CLOSED"),
            "CLOSED", Set.of(),
            "CANCELLED", Set.of()
    );

    public void validate(String currentStatus, String requestedStatus) {

        Set<String> allowed = ALLOWED_TRANSITIONS.get(currentStatus);

        if (allowed == null || !allowed.contains(requestedStatus)) {
            throw new InvalidStatusTransitionException(
                    currentStatus,
                    requestedStatus
            );
        }
    }
}
