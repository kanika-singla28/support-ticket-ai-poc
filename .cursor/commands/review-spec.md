# Review spec

Review `spec/` (and only those files unless the user names others). Do not edit files unless asked.

## Checks

1. **Acceptance coverage:** Every assignment criterion has a requirement ID and a testable `AC-*` or test-strategy bullet.
2. **Ambiguity:** Flag “meaningful”, “where applicable”, “if needed” without a pass/fail rule.
3. **Contradictions:** Field names, required vs optional, lengths, paths, status codes, RAG metadata, re-ingestion, versions.
4. **Testability:** Each `FR-*` / `RAG-*` / `NFR-*` can be observed in a test or measurement.
5. **Edge cases:** Terminal tickets, unassigned assignee, empty search, threshold boundaries, rollback, concurrent `@Version`, oversized fields, blank comments.
6. **NFRs:** Configurable top-K/threshold, no secrets, MySQL availability if PGVector is down, no Bootcamp migration.
7. **RAG grounding:** Retrieved-context-only; citation subset; no-match skips LLM; no Bootcamp knowledge; stale-index replacement.
8. **Scope creep:** SLA, attachments, email, agents, brokers, dashboards, extra entities.

## Output

| ID / topic | Issue | Severity | Suggested spec change |
|---|---|---|---|
| | | missing AC / ambiguity / contradiction / untestable / NFR / grounding / creep | |

Verdict: **READY FOR HUMAN APPROVAL** or **CHANGES REQUIRED** (list required vs optional separately).
