# Support Ticket Management System — UI Flow

## 1. Purpose

This document defines the frontend flows required for the Support Ticket Management System.

The React frontend consumes the Spring Boot REST APIs and must display meaningful backend validation and operation errors.

The backend remains authoritative for validation and ticket state transitions.

## 2. Main UI Capabilities

The UI must allow the user to:

- create a ticket
- list tickets
- search tickets
- filter tickets by status
- view complete ticket details
- update editable ticket fields
- change the assignee
- add comments
- perform valid status transitions
- ask natural-language questions about ticket history
- view AI source ticket IDs
- distinguish grounded answers from no-match responses

## 3. Ticket List Flow

Flow:

    Open ticket list
        ↓
    GET /api/tickets
        ↓
    Render returned tickets
        ↓
    User may search or filter
        ↓
    GET /api/tickets?search=...&status=...

The list must display enough information to identify and inspect tickets.

At minimum:

- ticket ID
- title
- priority
- status
- category
- assignee when available

An empty result is displayed as an empty-state message and not as an application failure.

## 4. Create Ticket Flow

Flow:

    User selects Create Ticket
        ↓
    Enter required ticket fields
        ↓
    Client-side usability validation
        ↓
    POST /api/tickets
        ↓
    Backend validation
        ↓
    Success or meaningful error

Required creation fields:

- title
- description
- priority
- category

On success, the UI must make the newly created ticket visible.

Backend validation errors must be displayed to the user.

## 5. Ticket Detail Flow

Flow:

    User selects a ticket
        ↓
    GET /api/tickets/{ticketId}
        ↓
    Render complete ticket details

The detail view must use the ticket-detail response rather than assuming the ticket-list response contains the complete ticket history.

The detail view must display, when available:

- ticket ID
- title
- description
- priority
- status
- category
- assignee
- resolution information
- comments
- creation timestamp
- update timestamp

The UI must not silently omit fields returned by the backend.

If required detail data is missing from the backend contract or response mapping, that is treated as an implementation defect rather than hidden by the frontend.

## 6. Update Ticket Flow

Editable fields are:

- title
- description
- priority
- assignee
- resolution information

Flow:

    Open ticket details
        ↓
    Edit supported fields
        ↓
    PATCH /api/tickets/{ticketId}
        ↓
    Backend validation
        ↓
    Refresh displayed ticket details

The UI must render the authoritative updated response after a successful update.

## 7. Comment Flow

Flow:

    Open ticket details
        ↓
    Enter comment
        ↓
    POST /api/tickets/{ticketId}/comments
        ↓
    Comment persisted
        ↓
    Refresh ticket details

Blank comments must not be accepted.

Backend validation errors must be visible to the user.

## 8. Status Transition Flow

The UI may present only transitions that are valid for the currently displayed state.

However, backend enforcement remains mandatory.

Supported transitions are:

    OPEN -> IN_PROGRESS
    OPEN -> CANCELLED
    IN_PROGRESS -> RESOLVED
    IN_PROGRESS -> CANCELLED
    RESOLVED -> CLOSED

If the backend rejects a transition, the returned error must be displayed rather than locally forcing the status change.

## 9. Search and Filter Flow

The UI provides:

- keyword search
- status filter

Search and filtering use the deterministic ticket endpoint:

`GET /api/tickets`

These controls are separate from the semantic AI question-answering feature.

Search and status filtering may be combined.

## 10. AI Question Flow

Flow:

    User enters natural-language question
        ↓
    POST /api/ai/ask
        ↓
    Backend performs semantic retrieval
        ↓
    Grounded response returned
        ↓
    UI displays answer and source tickets

The UI must display:

- generated answer
- whether relevant tickets were found
- cited ticket IDs

Source ticket IDs must come from the backend response.

The frontend must not generate or infer citations.

## 11. AI No-Match Flow

When the backend returns:

    relevantTicketsFound = false

the UI must clearly display the backend's no-relevant-ticket message.

It must not replace the response with a generic AI answer.

The sources area must remain empty when no sources are returned.

## 12. Error Handling

The UI must display meaningful errors for cases including:

- validation failure
- ticket not found
- invalid state transition
- invalid assignee
- backend unavailable
- AI retrieval or generation failure

Infrastructure failure must not be displayed as:

`No relevant tickets were found`

because no-match and system failure have different meanings.

## 13. Loading and Submission Behaviour

While an API operation is in progress, the UI should provide visible loading or submission feedback.

Repeated submissions should be prevented where practical.

After successful mutation operations, displayed data must be refreshed from authoritative backend results.

## 14. Existing Prototype Review Requirement

The existing standalone React prototype must be reviewed against this specification before being replaced or substantially rewritten.

Existing working UI code should be retained where it satisfies the specification.

A previously observed issue where the UI did not receive or display all required ticket data must be reproduced and investigated during implementation review.

The review must determine whether missing information originates from:

- backend entity/model gaps
- backend response DTO mapping
- list versus detail API usage
- frontend API response handling
- frontend rendering

The issue must be fixed at the responsible layer rather than compensated for with fabricated or hardcoded frontend data.

## 15. UI Acceptance Criteria

The UI is acceptable when:

- ticket creation works
- ticket listing works
- ticket details show all required available information
- ticket updates are reflected correctly
- assignee changes work
- comments can be added and displayed
- keyword search works
- status filtering works
- valid transitions can be requested
- backend transition errors are displayed
- meaningful validation errors are displayed
- AI questions can be submitted
- grounded AI answers are displayed
- source ticket IDs are displayed
- no-match AI responses are clearly represented
- infrastructure failures are distinguishable from no-match responses
