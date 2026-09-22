# Support Ticket — State Machine Specification

## 1. Purpose

This document defines the authoritative lifecycle rules for support ticket status transitions.

The state machine is deterministic business logic and must be enforced by the Spring Boot backend.

The frontend may guide the user toward valid transitions, but it is not the enforcement boundary.

The AI/LLM must not determine, override, or perform ticket state transitions.

## 2. States

The supported ticket states are:

- OPEN
- IN_PROGRESS
- RESOLVED
- CLOSED
- CANCELLED

Every newly created ticket starts in `OPEN`.

## 3. Allowed Transitions

The assignment defines the following transitions:

    OPEN -> IN_PROGRESS
    IN_PROGRESS -> RESOLVED
    RESOLVED -> CLOSED

Cancellation is allowed from:

    OPEN -> CANCELLED
    IN_PROGRESS -> CANCELLED

No other transition is valid.

## 4. Transition Matrix

| Current State | Target State | Allowed |
|---|---|---|
| OPEN | IN_PROGRESS | Yes |
| OPEN | CANCELLED | Yes |
| IN_PROGRESS | RESOLVED | Yes |
| IN_PROGRESS | CANCELLED | Yes |
| RESOLVED | CLOSED | Yes |

Any state/target combination not listed above is rejected.

## 5. Explicit Invalid Examples

The assignment explicitly identifies these as invalid:

    CLOSED -> OPEN
    RESOLVED -> OPEN
    CANCELLED -> OPEN

Other transitions outside the allowed transition set are also invalid.

Examples include:

    OPEN -> RESOLVED
    OPEN -> CLOSED
    IN_PROGRESS -> CLOSED
    RESOLVED -> CANCELLED
    CLOSED -> CANCELLED

## 6. Backend Enforcement

Status changes are performed through:

`PATCH /api/tickets/{ticketId}/status`

The backend must:

1. Load the authoritative ticket.
2. Validate that the requested target is a supported status.
3. Determine the ticket's current status.
4. Validate the current-to-target transition.
5. Reject an invalid transition without changing the ticket.
6. Persist a valid transition.
7. Trigger vector refresh because status is RAG metadata.

A valid status value used in an invalid transition returns `409 Conflict`.

An unsupported status value returns `400 Bad Request`.

## 7. Terminal States

`CLOSED` and `CANCELLED` have no outgoing transitions in the defined state machine.

A ticket in either state remains terminal unless the requirements are explicitly changed in a future specification revision.

## 8. RAG Interaction

Ticket status is included in vector metadata.

After a successful transition, the affected ticket must be re-ingested or its vector representation replaced so retrieval does not use stale status metadata.

The RAG system may describe ticket status when supported by retrieved ticket data.

It must not change ticket status.

## 9. Required Tests

Integration tests must cover every allowed transition:

- OPEN -> IN_PROGRESS
- OPEN -> CANCELLED
- IN_PROGRESS -> RESOLVED
- IN_PROGRESS -> CANCELLED
- RESOLVED -> CLOSED

Tests must also verify representative invalid transitions, including the assignment examples:

- CLOSED -> OPEN
- RESOLVED -> OPEN
- CANCELLED -> OPEN

Tests must verify that an invalid transition does not modify the persisted ticket state.

Terminal-state behavior must also be tested.
