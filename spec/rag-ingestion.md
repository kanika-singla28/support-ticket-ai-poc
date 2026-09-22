# Support Ticket RAG — Ingestion Specification

## 1. Purpose

This document defines how authoritative support-ticket data is converted into searchable knowledge for Retrieval-Augmented Generation (RAG).

The ingestion pipeline must ensure that:

- embeddings represent real ticket data
- ticket sources remain traceable
- comments and resolution notes become searchable
- updated tickets do not leave stale knowledge behind
- vector metadata contains the fields required by the assignment
- vector data remains derived from authoritative relational data

## 2. Authoritative Source

PostgreSQL relational ticket data is the source of truth.

PGVector contains a derived representation used only for semantic retrieval.

The ingestion pipeline reads authoritative ticket data and creates vector knowledge documents from it.

The vector store must never independently create or modify ticket information.

## 3. Knowledge Sources

The assignment requires the following ticket information to become searchable knowledge:

- description
- comments
- resolution notes

Ticket title is also included because it provides concise issue context for retrieval.

The knowledge representation therefore uses:

- ticket title
- ticket description
- ticket comments
- resolution information

Fields such as status, priority, assignee and category are primarily represented as metadata.

## 4. Required Metadata

Every vector document must contain metadata identifying its authoritative source.

Required metadata:

| Metadata | Purpose |
|---|---|
| ticketId | Source attribution and ticket lookup |
| status | Ticket-state context and filtering |
| priority | Priority context and filtering |
| assignee | Assignee context and filtering |
| category | Ticket-category context and filtering |

Metadata must come from authoritative ticket data.

The LLM must not generate or infer ticket IDs used for source attribution.

## 5. Document Construction

Each ticket is transformed into one or more knowledge documents.

A logical ticket representation contains labelled sections such as:

    Ticket: TKT-1001
    Title: Payment failure during checkout
    Description: Customer payment fails after gateway timeout.
    Comments:
    - Retry succeeded after gateway recovery.
    Resolution:
    Payment gateway timeout was the root cause.

Labels are retained because they provide semantic structure to the embedding and generation context.

Ticket identity must remain associated with every generated chunk through metadata.

## 6. Chunking Strategy

Support tickets are structured records rather than large unstructured documents.

The POC therefore uses a ticket-aware chunking strategy instead of blindly splitting every ticket into fixed-size chunks.

For normal-sized tickets, related ticket information should remain together where practical so retrieval preserves the meaning of the issue and its resolution.

Logical content boundaries are:

- ticket title and description
- comments
- resolution information

For larger ticket histories, content may be split at logical boundaries such as individual comments or paragraphs.

Every resulting chunk must retain the same source-ticket metadata, including `ticketId`.

Fixed-size splitting should only be used as a fallback when a logical section is too large for practical embedding or generation context.

This approach was selected because ticket records are generally short and structured, and separating closely related issue and resolution information unnecessarily can reduce retrieval usefulness.

## 7. Embedding Model

The POC uses the local Ollama embedding model:

`nomic-embed-text`

The current embedding dimension is:

`768`

The model was selected for the standalone POC because it:

- runs locally
- does not require a cloud API key
- avoids per-request embedding cost
- keeps ticket content local
- integrates with Spring AI
- is sufficient for demonstrating semantic retrieval

The trade-off is that a local embedding model may provide different retrieval quality and performance characteristics than larger hosted embedding models.

Embedding quality must therefore be evaluated using representative support-ticket questions rather than assumed to be correct.

## 8. Vector Store

PGVector is used as the vector store.

It runs on PostgreSQL with the `vector` extension enabled.

The current vector representation uses:

- 768-dimensional embeddings
- cosine distance
- HNSW indexing

Spring AI manages vector-store integration.

The vector store contains derived knowledge and can be rebuilt from authoritative ticket data if necessary.

## 9. Ingestion Triggers

A ticket must be ingested when it first becomes part of the searchable ticket knowledge base.

The ticket must be refreshed when information affecting either searchable content or required metadata changes.

Refresh triggers include:

- ticket title update
- ticket description update
- priority update
- assignee update
- category update when supported
- resolution information update
- comment addition
- status transition

This ensures that semantic retrieval represents the latest authoritative ticket state.

## 10. Re-Ingestion and Stale Vector Prevention

Re-ingestion must replace the existing vector representation for the affected ticket rather than continuously appending new copies.

The refresh flow is:

    Ticket changed
        ↓
    Load latest authoritative ticket data
        ↓
    Remove existing vector documents for ticketId
        ↓
    Rebuild ticket knowledge documents
        ↓
    Generate new embeddings
        ↓
    Store refreshed documents in PGVector

All chunks belonging to a ticket must be identifiable using `ticketId` metadata.

The replacement operation must prevent obsolete descriptions, comments, resolution information, or metadata from remaining searchable after an update.

If multiple chunks exist for one ticket, all stale chunks for that ticket must be removed before or as part of replacement.

## 11. Retrieval Configuration

Retrieval parameters must be externally configurable.

The current configuration properties are:

    poc.rag.top-k=${RAG_TOP_K:5}
    poc.rag.similarity-threshold=${RAG_SIMILARITY_THRESHOLD:0.75}

Default values are:

- Top-K: 5
- Similarity threshold: 0.75

These values are initial POC defaults rather than assumptions of optimal retrieval quality.

They must be evaluated and may be tuned using representative support-ticket questions.

Application code must read these values from configuration rather than hardcoding them.

## 12. Retrieval Metadata

Retrieved documents must preserve their source metadata.

At minimum, retrieved context must make the following information available:

- ticketId
- status
- priority
- assignee
- category

The application uses `ticketId` from retrieved metadata to construct source citations returned by the AI API.

Source ticket IDs must come from retrieved documents and must never be invented by the language model.

## 13. Ingestion Failure Behaviour

Failure to generate or store an embedding must not silently create the impression that the vector knowledge base is current.

An ingestion failure must be observable through application logging or error handling.

The authoritative relational ticket must remain valid even if derived vector ingestion fails.

The implementation must avoid deleting authoritative ticket data as part of vector refresh.

Failure-handling behavior must be tested where practical.

## 14. Ingestion Scope

The ingestion pipeline is responsible only for creating and refreshing searchable ticket knowledge.

It does not:

- answer user questions
- perform autonomous actions
- modify ticket lifecycle state
- create support tickets on behalf of the LLM
- send notifications
- call external business tools

Question answering is handled separately by the RAG retrieval-and-generation flow defined in `rag-api-contract.md`.
