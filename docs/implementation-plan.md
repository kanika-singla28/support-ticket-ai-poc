# Implementation Plan

## Existing Implementation

The standalone POC already provides:

- Java 21 / Spring Boot backend
- PostgreSQL persistence
- PGVector
- Ollama embeddings
- Ollama chat model
- ticket creation
- initial ticket vector ingestion
- POST /api/ai/ask
- React create-ticket UI
- React AI-question UI

## Identified Gaps

### Domain Model

Add:

- assignee
- resolution information
- updatedAt
- comments

### Ticket APIs

Implement:

- list tickets
- ticket details
- update ticket
- keyword search
- status filtering
- comments
- status transitions

### State Machine

Enforce:

OPEN -> IN_PROGRESS
OPEN -> CANCELLED
IN_PROGRESS -> RESOLVED
IN_PROGRESS -> CANCELLED
RESOLVED -> CLOSED

Reject all other transitions.

### RAG

Retain existing Spring AI + PGVector implementation.

Extend ingestion to include:

- comments
- resolution information
- assignee metadata

Implement re-ingestion after:

- ticket update
- comment addition
- status transition

Review:

- stale-vector deletion
- grounding
- insufficient-evidence handling
- source integrity

### Frontend

Extend the existing React implementation rather than replacing it.

Add:

- ticket list
- search
- status filter
- ticket details
- update
- assignee
- comments
- status transitions
- complete error handling

Fix:

- remove unsupported CRITICAL priority
- remove stale MySQL reference
- display complete backend ticket data

### Testing

Add:

- CRUD tests
- validation tests
- state-machine integration tests
- search/filter tests
- RAG ingestion tests
- re-ingestion tests
- retrieval/no-match tests
- grounding/source tests
- missing-data regression test
