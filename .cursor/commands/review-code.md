# Review code

Review the current AI-assisted implementation (diff and related files). Do not implement fixes unless asked. Read `spec/` first.

## Checks

1. **Specification:** Behaviour matches `spec/requirements.md`, `spec/api-contract.md`, `spec/state-machine.md`, `spec/rag-api-contract.md`, `spec/rag-ingestion.md`. Cite IDs (`FR-*`, `RAG-*`, `AC-*`).
2. **Architecture:** MySQL is SoR; PGVector is derived; after-commit indexing; no extra stores/brokers. Dual-store failure must not roll back committed tickets.
3. **Security:** Reuse existing auth; no new permit-all endpoints without `@PreAuthorize`; no secrets in code/logs/responses; no SQL/prompt/provider leakage.
4. **Validation:** Bean Validation + length limits; optional assignee; no create-time resolution; terminal tickets reject mutation with `TERMINAL_TICKET_READ_ONLY`.
5. **Transactions:** Ticket change and `PENDING` indexing intent in one MySQL transaction. Embedding/PGVector only after commit. Rollback drops intent.
6. **Error handling:** Stable codes, existing `CommonResponseVO`, correct HTTP statuses. No-match is `200`, not `5xx`.
7. **Tests:** Mapped to IDs; positive/negative/boundary; state-machine matrix; RAG no-match/citations. Flag missing or tautological tests.
8. **Scope creep:** Reject SLA, attachments, email, agents, FAQs, Kafka/RabbitMQ/Quartz, admin retry APIs, extra roles, extra ticket fields.
9. **Hallucinated APIs:** Verify every imported class, Spring annotation, and dependency exists in this repo or an **already approved** planned library. Flag invented methods.

## Output

- Critical (must fix)
- Major
- Minor
- Scope creep
- Spec gaps discovered in code
- Verdict: **approve** | **request changes**
