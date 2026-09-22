# Support Ticket Management System — Test Strategy

## 1. Purpose

This document defines the testing strategy for the Support Ticket Management System.

Testing covers two distinct areas:

1. deterministic application behaviour
2. probabilistic RAG behaviour

Deterministic tests verify exact business and API rules.

RAG tests verify retrieval, grounding, citations, no-match behaviour and knowledge freshness.

Detailed RAG quality evaluation is defined in `evaluation-strategy.md`.

## 2. Testing Levels

The project uses:

- unit tests for isolated business logic
- repository tests where database query behaviour requires verification
- integration tests for API and persistence behaviour
- state-machine integration tests
- RAG ingestion tests
- retrieval tests
- grounded-answer evaluation
- frontend behaviour testing where practical
- manual end-to-end verification for the completed POC

## 3. Ticket Creation Tests

Tests must verify:

- valid ticket creation succeeds
- new ticket starts in `OPEN`
- required fields are persisted
- blank title is rejected
- blank description is rejected
- blank category is rejected
- unsupported priority is rejected
- persisted data survives subsequent retrieval

## 4. Ticket List, Search and Filter Tests

Tests must verify:

- tickets can be listed
- keyword search returns matching tickets
- status filtering returns matching tickets
- search and status filtering work together
- no matches return an empty result
- invalid status input is rejected

Search tests apply to deterministic database search and are separate from semantic RAG retrieval tests.

## 5. Ticket Detail Tests

Tests must verify that the detail API returns the complete available ticket representation, including:

- ID
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

A nonexistent ticket returns `404 Not Found`.

The previously observed missing-data issue must receive a regression test once its root cause is identified.

## 6. Ticket Update Tests

Tests must verify updates to:

- title
- description
- priority
- assignee
- resolution information

Tests must verify:

- partial updates do not unintentionally overwrite unspecified fields
- invalid values are rejected
- nonexistent tickets return `404 Not Found`
- invalid assignee input produces a meaningful error
- successful RAG-relevant updates trigger knowledge refresh

## 7. Comment Tests

Tests must verify:

- a valid comment can be added
- blank comment content is rejected
- comment is associated with the correct ticket
- nonexistent ticket is rejected
- added comment appears in ticket details
- adding a comment triggers RAG re-ingestion

## 8. State-Machine Tests

Every allowed transition must be tested:

    OPEN -> IN_PROGRESS
    OPEN -> CANCELLED
    IN_PROGRESS -> RESOLVED
    IN_PROGRESS -> CANCELLED
    RESOLVED -> CLOSED

Representative invalid transitions must include:

    CLOSED -> OPEN
    RESOLVED -> OPEN
    CANCELLED -> OPEN

Tests must also verify:

- invalid transition returns `409 Conflict`
- persisted status remains unchanged after rejection
- CLOSED has no outgoing transitions
- CANCELLED has no outgoing transitions
- successful status changes refresh RAG metadata

These tests satisfy the assignment requirement for state-machine integration testing.

## 9. Backend Validation Tests

Validation tests must verify that malformed or unsupported input is rejected by the backend even if frontend validation is bypassed.

The backend is the authoritative validation boundary.

Errors must contain information suitable for meaningful UI feedback.

## 10. Persistence Tests

PostgreSQL is the authoritative persistent store.

Tests or end-to-end verification must demonstrate that ticket data survives application restart.

PGVector is derived data and must not replace relational persistence tests.

## 11. RAG Ingestion Tests

Tests must verify that ticket knowledge is constructed from the required data:

- title
- description
- comments
- resolution information

Vector metadata must include:

- ticketId
- status
- priority
- assignee
- category

Tests must verify that metadata corresponds to authoritative ticket data.

## 12. Re-Ingestion Tests

Tests must verify that updates refresh the vector representation.

Representative flow:

    Create and ingest ticket
        ↓
    Confirm original knowledge
        ↓
    Update ticket
        ↓
    Re-ingest
        ↓
    Confirm updated knowledge
        ↓
    Confirm obsolete knowledge is not retained

Tests must cover refresh caused by:

- ticket update
- comment addition
- status transition

Duplicate stale chunks for the same previous ticket representation must not remain searchable.

## 13. Retrieval Tests

Retrieval tests must execute semantic search independently from final answer generation where practical.

Tests verify:

- known questions retrieve expected ticket evidence
- `ticketId` metadata is preserved
- Top-K configuration is respected
- similarity threshold configuration is respected
- irrelevant questions can produce no qualifying documents

Exact retrieval-quality evaluation cases are defined in `evaluation-strategy.md`.

## 14. AI Grounding Tests

For retrieved ticket context, tests or controlled evaluations must verify that generated answers:

- use only supplied ticket evidence
- do not invent ticket facts
- do not invent root causes
- do not invent resolutions
- do not invent source ticket IDs
- acknowledge insufficient evidence

Generated fluency alone is not a passing criterion.

## 15. Citation Tests

Tests must verify:

- relevant answers contain source ticket IDs
- source IDs originate from retrieved metadata
- duplicate chunks do not create duplicate source citations
- no-match responses contain no sources
- generated text is not trusted as the source of citation IDs

## 16. No-Match and Hallucination Tests

At least one evaluation question must have no relevant ticket in the knowledge base.

Expected behaviour:

    relevantTicketsFound = false
    sources = []
    explicit no-relevant-ticket answer

The model must not answer the support-specific question using general model knowledge.

A separate insufficient-evidence case must verify that related retrieval does not cause unsupported conclusions.

## 17. Infrastructure Failure Tests

Where practical, verify behaviour when:

- PostgreSQL is unavailable
- PGVector retrieval fails
- Ollama embedding service is unavailable
- Ollama generation model is unavailable

Infrastructure failures must not be reported as successful no-match results.

Secrets and internal stack traces must not be exposed to the UI.

## 18. Frontend Verification

Frontend verification must cover the acceptance flows:

- create ticket
- list tickets
- view complete details
- update ticket
- change assignee
- add comment
- search
- filter by status
- transition status
- display backend validation errors
- ask AI question
- display AI sources
- display no-match response
- distinguish AI infrastructure errors from no-match results

Existing React code should be tested and retained when it satisfies these flows.

## 19. Test Data

Tests must use controlled ticket fixtures with known expected outcomes.

Fixture data should include examples for:

- payment failures
- shipment or tracking issues
- multiple priorities
- multiple statuses
- resolved tickets with resolution information
- tickets with comments
- unrelated content for negative retrieval testing

Test data must not depend on invented production records.

## 20. Review and Fix Cycle

A successful test run does not end the engineering workflow.

After implementation:

    Run tests
        ↓
    Review deterministic failures
        ↓
    Review retrieval quality
        ↓
    Review AI answers for grounding
        ↓
    Document meaningful AI mistakes
        ↓
    Fix implementation or configuration
        ↓
    Re-run tests and evaluation

This implements the required:

Implementation → Testing → Review → Fix

workflow.

## 21. Completion Criteria

Testing is complete when:

- deterministic acceptance behaviour passes
- state-machine integration tests pass
- persistence is demonstrated
- the missing-data UI regression is resolved and verified
- RAG ingestion is verified
- re-ingestion prevents stale searchable knowledge
- expected tickets are retrieved for controlled evaluation cases
- no-match behaviour is verified
- insufficient-evidence behaviour is verified
- source attribution is verified
- accepted AI answers contain no unsupported ticket-specific claims
- meaningful AI mistakes and their corrections are documented
