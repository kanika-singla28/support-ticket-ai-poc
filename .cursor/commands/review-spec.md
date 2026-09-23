# Review Specification

Review `spec/` for completeness, consistency, and testability. Do not edit files unless asked.

## Checks

1. **Acceptance coverage:** Every assignment criterion should map to a requirement, acceptance criterion, or explicit test/evaluation obligation.
2. **Ambiguity:** Flag words such as "meaningful", "where applicable", or "if needed" when no observable pass/fail rule exists.
3. **Contradictions:** Check field names, required/optional behaviour, validation limits, API paths, status codes, state transitions, RAG metadata, retrieval behaviour, and re-ingestion requirements.
4. **Testability:** Each functional, RAG, and non-functional requirement should be observable through a deterministic test or documented evaluation.
5. **Ticket edge cases:** Check unassigned tickets, empty searches, missing tickets, malformed requests, invalid transitions, and terminal states.
6. **RAG edge cases:** Check threshold boundaries, no-match behaviour, stale knowledge, unsupported generated claims, fabricated ticket references, and duplicate/obsolete indexed content.
7. **RAG configuration:** Top-K and similarity threshold must be configurable.
8. **Grounding:** Generated support-ticket answers must use retrieved context only; no-match must skip generation; returned sources must come from retrieved qualifying tickets.
9. **Architecture:** Ensure the specification consistently describes the standalone PostgreSQL + PGVector design and the configured Spring AI/model integration.
10. **Security:** Specifications must not require committed credentials or expose secrets/provider internals.
11. **Scope:** Flag autonomous agents, unrelated tools/actions, notifications, SLA workflows, attachments, brokers, or other features outside the assignment.

## Output

| ID / Topic | Issue | Severity | Suggested specification change |
|---|---|---|---|
| | | | |

Then list:

- Required changes
- Optional improvements
- Open questions requiring human decision
