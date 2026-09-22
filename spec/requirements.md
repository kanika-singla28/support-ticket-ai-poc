# Support Ticket Management System — Requirements

## 1. Purpose

Build an AI-powered Support Ticket Management System using spec-driven development.

The project must demonstrate both conventional application development and an AI-native Retrieval-Augmented Generation (RAG) capability.

The primary assessment is not only the finished application, but also how AI is used throughout the engineering lifecycle:

Requirement → Specification → Plan / Tasks → Implementation → Testing → Review → Fix

## 2. Technology Requirements

The solution will use:

- Java 21
- Spring Boot
- Spring AI
- PostgreSQL
- PGVector
- An embedding model
- REST APIs
- React frontend
- Cursor as the AI-assisted development environment

## 3. Ticket Management Requirements

The system must allow a user to:

- Create a support ticket.
- List support tickets.
- View ticket details.
- Update ticket title.
- Update ticket description.
- Update ticket priority.
- Change ticket assignee.
- Add comments to a ticket.
- Search tickets by keyword.
- Filter tickets by status.
- Persist ticket data across application restarts.

## 4. Ticket Fields

A support ticket must contain sufficient information to support CRUD operations and RAG retrieval.

Required business information includes:

- Ticket ID
- Title
- Description
- Priority
- Status
- Assignee
- Category
- Comments
- Resolution information
- Creation timestamp
- Update timestamp

## 5. Ticket State Machine

The backend must enforce the following transitions:

OPEN → IN_PROGRESS

IN_PROGRESS → RESOLVED

RESOLVED → CLOSED

OPEN → CANCELLED

IN_PROGRESS → CANCELLED

Transitions outside this state machine must be rejected.

Examples of invalid transitions:

- CLOSED → OPEN
- RESOLVED → OPEN
- CANCELLED → OPEN

The backend is authoritative for transition validation. The UI must not be relied upon to enforce business rules.

## 6. Backend Validation

Backend APIs must validate incoming data.

Validation failures must return meaningful API errors.

Examples include:

- Missing title
- Missing description
- Missing priority
- Missing category
- Invalid status transition
- Unknown ticket
- Invalid request data

The UI must display meaningful errors returned by the backend.

## 7. AI Question-Answering Requirement

The application must expose:

POST /api/ai/ask

Example request:

{
  "question": "What caused previous payment failures?"
}

The assistant must answer natural-language questions using retrieved support-ticket history.

## 8. RAG Ingestion

Ticket knowledge must be converted into searchable documents.

Knowledge may include:

- Ticket description
- Comments
- Resolution information

Each indexed document must contain metadata sufficient to identify and filter its source.

Required metadata:

- ticketId
- status
- priority
- assignee
- category

Ticket embeddings must be refreshed when relevant ticket information changes so that the vector knowledge base does not become stale.

## 9. RAG Retrieval

The retrieval pipeline must follow:

Support Tickets
↓
Create Knowledge Documents
↓
Chunk
↓
Generate Embeddings
↓
Vector Store
↓
User Question
↓
Similarity Search
↓
Relevant Tickets
↓
LLM + Retrieved Context
↓
Grounded Answer
↓
Ticket Sources

Retrieval configuration must include:

- Configurable Top-K
- Configurable similarity threshold

These values must not be hardcoded in application logic.

## 10. Grounding and Hallucination Guardrails

The assistant must answer support-specific questions only from retrieved ticket context.

The assistant must not fall back to general model knowledge when answering questions about ticket history.

Every grounded answer must identify the ticket source or sources used.

If no sufficiently relevant tickets are retrieved, the API must explicitly indicate that no relevant tickets were found.

The assistant must not fabricate a plausible support answer when relevant evidence is unavailable.

## 11. RAG Scope

The AI functionality is a single:

retrieval → generation

flow.

It is not an autonomous agent.

The assistant must not independently:

- Create tickets
- Update tickets
- Send notifications
- Invoke unrelated tools
- Chain autonomous actions

## 12. Example Questions

The system should support questions such as:

- Have we seen payment failures before?
- What was the resolution for ticket TKT-1001?
- What are the common causes of shipment tracking issues?
- Show me similar resolved tickets.
- Which high-priority tickets are related to payment?

## 13. Retrieval Quality Documentation

The project must document:

- Ticket-specific chunking strategy
- Embedding model choice
- Local versus cloud model considerations
- Cost trade-offs
- Latency trade-offs
- Quality trade-offs
- Top-K rationale
- Similarity-threshold rationale

## 14. Testing Requirements

Testing must cover deterministic application behavior and probabilistic AI behavior.

At minimum:

- Ticket creation
- Ticket validation
- Ticket update
- Comments
- Search
- Status filtering
- Valid state transitions
- Invalid state transitions
- Persistence
- RAG ingestion
- Re-ingestion after ticket updates
- Retrieval
- Source citation
- No-match behavior
- Grounding / hallucination behavior

State-machine integration tests are required.

## 15. UI Requirements

The frontend must allow a user to:

- Create tickets
- List tickets
- View ticket details
- Update ticket fields
- Change assignee
- Add comments
- Search tickets
- Filter tickets by status
- Change ticket status
- Ask natural-language questions about ticket history
- View AI answers
- View cited ticket IDs
- See when no relevant tickets were found
- See meaningful backend validation/errors

## 16. AI-Assisted Development Requirements

Reusable AI instructions must be maintained for:

- Java / Spring Boot development
- Testing
- API standards
- Documentation
- RAG / vector-store conventions
- Chunking conventions
- Embedding-model decisions
- Retrieval tuning

Reusable commands must exist for:

- Code review
- Specification review
- Test generation
- RAG-output review for hallucinated or ungrounded answers

## 17. Prompt History

AI prompts used during development must be retained.

The repository must contain:

.specstory/history/

and:

docs/prompt-history.md

Prompt history should demonstrate how AI was used throughout specification, implementation, testing, review and correction.

## 18. AI Mistake Review

At least one meaningful incorrect AI suggestion or AI-generated mistake must be identified, reviewed and documented.

This can include:

- Incorrect generated code
- Incorrect architecture advice
- Incorrect API assumptions
- Hallucinated RAG answers
- Ungrounded assistant responses

The documentation must explain:

1. What AI suggested.
2. Why it was incorrect.
3. How it was detected.
4. What was changed.
5. What engineering lesson was learned.

## 19. Security

No passwords, API keys, tokens or other secrets may be committed to the repository.

Runtime secrets must be supplied through environment variables or another external configuration mechanism.

## 20. Core Acceptance Criteria

The solution is complete when:

- Ticket can be created from UI.
- Tickets can be listed.
- Ticket details can be viewed.
- Ticket fields can be updated.
- Assignee can be changed.
- Comments can be added.
- Search works.
- Status filter works.
- Valid status transitions work.
- Invalid status transitions are rejected by backend.
- Data survives application restart.
- Backend validation works.
- UI displays meaningful errors.
- State-machine integration tests pass.
- Ticket data is converted into embeddings.
- Embeddings are stored in PGVector.
- POST /api/ai/ask returns grounded answers for relevant questions.
- AI responses cite the specific ticket IDs used.
- No-match questions explicitly report that no relevant tickets were found.
- Chunking strategy is documented and justified.
- Embedding-model choice is documented and justified.
- Ticket updates trigger re-ingestion.
- Top-K is configurable.
- Similarity threshold is configurable.
- No secrets are committed.
- At least one meaningful AI mistake is documented.
