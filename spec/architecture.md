# Support Ticket Management System — Architecture

## 1. Purpose

This document defines the architecture for the standalone AI-Powered Support Ticket Management System.

The system combines conventional support-ticket management with a Retrieval-Augmented Generation (RAG) capability. The AI capability is part of the system architecture from the beginning rather than being added as an independent chatbot.

## 2. Architecture Goals

The architecture must:

- Support persistent ticket CRUD operations.
- Enforce ticket lifecycle rules in the backend.
- Support comments, assignees, search and filtering.
- Convert ticket history into searchable vector knowledge.
- Keep vector knowledge synchronized with ticket changes.
- Answer questions strictly from retrieved ticket evidence.
- Cite ticket IDs used in AI answers.
- Explicitly handle cases where relevant evidence is unavailable.
- Keep retrieval configuration externalized.
- Keep secrets outside source control.
- Remain small enough to demonstrate clearly as a POC.

## 3. High-Level Architecture

```text
┌─────────────────────────────┐
│        React Frontend       │
│                             │
│ Ticket Management           │
│ Search / Filter             │
│ AI Question Interface       │
└──────────────┬──────────────┘
               │ REST / JSON
               ▼
┌─────────────────────────────┐
│       Spring Boot API       │
│                             │
│ Controllers                 │
│ Validation                  │
│ Ticket Services             │
│ State Machine               │
│ RAG Services                │
└───────┬─────────────┬───────┘
        │             │
        │ JPA         │ Spring AI
        ▼             ▼
┌───────────────┐   ┌──────────────────┐
│ PostgreSQL    │   │ Ollama           │
│               │   │                  │
│ Ticket Data   │   │ nomic-embed-text │
│ Comments      │   │ llama3.2:1b      │
│ Assignees     │   └────────┬─────────┘
└───────┬───────┘            │
        │                    │ embeddings /
        │                    │ generation
        ▼                    ▼
┌─────────────────────────────┐
│ PGVector                    │
│                             │
│ vector_store                │
│ document content            │
│ metadata                    │
│ 768-dimensional embeddings  │
└─────────────────────────────┘

## 4. Backend

The backend uses Java 21, Spring Boot, Spring Web, Spring Validation, Spring Data JPA and Spring AI.

Backend responsibilities:
- REST APIs
- Input validation
- Ticket CRUD
- Ticket lifecycle enforcement
- Comments
- Search and filtering
- Relational persistence
- RAG ingestion and retrieval
- Grounded AI response generation

Business rules remain in the backend and are not delegated to the frontend or LLM.

## 5. Frontend

The frontend uses React and communicates with the backend using REST APIs.

It supports ticket creation, listing, details, updates, comments, search, status filtering, status transitions and natural-language questions.

The UI displays validation errors, business errors, AI answers and ticket citations.

The backend remains authoritative for validation and business rules.

## 6. Persistence Architecture

PostgreSQL is the system of record for support-ticket data.

It stores tickets, comments, status, assignee information, resolution information and timestamps.

Spring Data JPA is used for relational persistence.

PostgreSQL was selected instead of using both MySQL and PostgreSQL because PGVector already requires PostgreSQL. A single database technology reduces unnecessary infrastructure while still separating relational ticket tables from vector data.

The relational ticket data remains authoritative.

## 7. Vector Store

PGVector is used through Spring AI for vector storage.

Vector documents contain:
- Searchable ticket content
- Embeddings
- ticketId
- status
- priority
- assignee
- category

PGVector contains derived searchable knowledge. It is not the authoritative ticket database.

## 8. Embedding Model

The POC uses `nomic-embed-text` through local Ollama.

The embedding dimension is 768.

Reasons for using a local embedding model:
- No external API key
- No cloud inference cost
- Ticket data remains local
- Simple and reproducible POC environment

Trade-offs include local CPU/RAM usage and potentially lower quality than larger hosted embedding models.

## 9. Generation Model

The POC uses `llama3.2:1b` through local Ollama.

It was selected because it runs locally, requires no cloud credentials and is sufficient for demonstrating grounded RAG generation.

The model is not treated as a source of support knowledge. Retrieved ticket evidence is the source of truth.

## 10. RAG Architecture

The RAG flow is:

Ticket Data
→ Knowledge Documents
→ Chunking
→ Embeddings
→ PGVector
→ User Question
→ Similarity Search
→ Relevant Ticket Context
→ LLM
→ Grounded Answer
→ Ticket Sources

This is a single retrieval-to-generation flow, not an autonomous agent.

## 11. Knowledge Construction

Searchable ticket knowledge is constructed from:
- Title and description
- Comments
- Resolution information

Metadata is attached to each vector document for attribution and retrieval.

Detailed chunking rules are defined in `rag-ingestion.md`.

## 12. Re-Ingestion Strategy

Vector knowledge must remain synchronized with authoritative ticket data.

Re-ingestion is required when relevant ticket information changes, including:
- Title
- Description
- Priority
- Status
- Assignee
- Category
- Comments
- Resolution information
- Ticket closure

Existing vector documents for the affected ticket will be refreshed or replaced.

The relational database remains the source of truth.

## 13. Retrieval Configuration

RAG retrieval parameters must be configurable rather than hardcoded.

Initial POC defaults:
- Top-K: 5
- Similarity threshold: 0.75

These are starting values and may be adjusted after retrieval-quality evaluation.

Configuration is provided through application properties with environment-variable overrides.

## 14. Vector Index

The PGVector configuration uses:
- Embedding dimension: 768
- Distance type: cosine distance
- Index type: HNSW

The 768 dimensions match the selected `nomic-embed-text` embedding model.

Cosine distance is used for semantic similarity, while HNSW provides efficient nearest-neighbour retrieval.

## 15. Grounding Boundary

The assistant must answer support-history questions only from retrieved ticket context.

The generation step receives:
1. The user's question.
2. Retrieved ticket evidence.
3. Explicit grounding instructions.

If no ticket passes the configured relevance threshold, the system must not generate a support-specific answer from general LLM knowledge.

Instead, it returns an explicit no-relevant-ticket response.

## 16. Source Attribution

Every grounded AI answer must expose the ticket IDs used as evidence.

Ticket IDs are obtained programmatically from vector-document metadata.

They must not be invented by the LLM.

Example:

{
  "answer": "Previous payment failures were associated with gateway timeout errors.",
  "relevantTicketsFound": true,
  "sources": [
    {
      "ticketId": 1001
    }
  ]
}

## 17. No-Match Handling

If retrieval finds no sufficiently relevant ticket context, the response must explicitly indicate that no relevant tickets were found.

Example:

{
  "answer": "No relevant tickets were found for this question.",
  "relevantTicketsFound": false,
  "sources": []
}

The LLM must not fabricate a plausible support answer when supporting ticket evidence is unavailable.

## 18. Ticket State Machine

Ticket status transitions are deterministic business logic enforced by the Spring Boot backend.

Allowed transitions:

OPEN → IN_PROGRESS → RESOLVED → CLOSED

OPEN → CANCELLED

IN_PROGRESS → CANCELLED

Invalid transitions such as CLOSED → OPEN, RESOLVED → OPEN and CANCELLED → OPEN must be rejected.

The LLM is never responsible for deciding whether a status transition is valid.

Detailed rules are defined in `state-machine.md`.

## 19. Error Handling

The backend provides meaningful errors for:
- Validation failures
- Ticket not found
- Invalid status transitions
- Malformed requests
- Internal processing failures

The React frontend displays these errors in a user-readable form.

## 20. Security and Configuration

Secrets must not be committed to the repository.

Sensitive configuration is supplied using environment variables.

Examples include:
- Database password
- Any future cloud-model credentials

RAG settings such as Top-K and similarity threshold are configurable using application properties with environment-variable overrides.

## 21. Testing Architecture

The project requires testing for both deterministic and probabilistic behavior.

Deterministic testing covers:
- Ticket CRUD
- Backend validation
- State transitions
- Search
- Status filtering
- Persistence
- Re-ingestion behavior

AI and RAG evaluation covers:
- Retrieval relevance
- Grounding
- Ticket source attribution
- No-match behavior
- Hallucination resistance

Detailed testing approaches are defined in `test-strategy.md` and `evaluation-strategy.md`.

## 22. AI-Assisted Engineering

Reusable project instructions are maintained in:

rules/
- java-springboot.md
- testing.md
- api-standards.md
- rag-vector-store.md

skills/
- documentation/

commands/
- review-code.md
- review-spec.md
- generate-tests.md
- review-rag-output.md

These artifacts provide reusable AI engineering instructions instead of repeatedly supplying the same context in every prompt.

## 23. Prompt and Decision Traceability

Development prompts are retained under:

`.specstory/history/`

A human-readable prompt summary is maintained in:

`docs/prompt-history.md`

Meaningful AI mistakes and their corrections are documented in:

`docs/ai-mistakes.md`

This demonstrates that AI-generated output is reviewed rather than accepted blindly.

## 24. Current Validated Infrastructure

The following local infrastructure has already been successfully verified:

- Java 21 compilation
- Spring Boot startup
- PostgreSQL connectivity
- JPA repository initialization
- PGVector extension 0.8.6
- Spring AI PGVector auto-configuration
- `support_ticket` relational table
- `vector_store` vector table
- 768-dimensional vector column
- HNSW cosine index
- Ollama service availability
- `nomic-embed-text` model availability
- `llama3.2:1b` model availability

This confirms the technical feasibility of the selected architecture before completing the remaining application features.

## 25. Out of Scope

The following are intentionally outside this POC:

- Autonomous AI agents
- AI-created tickets without explicit user action
- Notification workflows
- Email integration
- External tool execution
- Multi-agent orchestration
- Cloud deployment
- Production-scale authentication and authorization
- LLM-driven state transitions

## 26. Architecture Decision Summary

| Decision | Choice | Reason |
|---|---|---|
| Runtime | Java 21 | Assignment requirement |
| Backend | Spring Boot | Assignment requirement |
| AI integration | Spring AI | Assignment requirement |
| Relational DB | PostgreSQL | Persistent CRUD and simplified infrastructure |
| Vector store | PGVector | PostgreSQL-native vector retrieval |
| Embeddings | nomic-embed-text | Local, private and zero API cost |
| Generation | llama3.2:1b | Lightweight local generation |
| Vector dimension | 768 | Matches selected embedding model |
| Similarity | Cosine | Semantic embedding comparison |
| Vector index | HNSW | Efficient nearest-neighbour retrieval |
| Frontend | React | Assignment-compatible frontend |
| AI pattern | Single RAG flow | Explicit assignment scope |
| Secrets | Environment variables | Prevent secrets from being committed |
