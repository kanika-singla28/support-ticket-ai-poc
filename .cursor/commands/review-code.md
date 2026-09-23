# Review Code

Review the current AI-assisted implementation, including the diff and related files. Do not implement fixes unless asked. Read the relevant `spec/` files first.

## Checks

1. **Specification:** Verify behaviour against `spec/requirements.md`, `spec/api-contract.md`, `spec/state-machine.md`, `spec/rag-api-contract.md`, and `spec/rag-ingestion.md`. Reference relevant `FR-*`, `RAG-*`, `NFR-*`, or `AC-*` IDs where available.
2. **Architecture:** Preserve the standalone layered design: controller → service → repository. PostgreSQL is authoritative ticket persistence; PGVector provides derived semantic retrieval.
3. **Security:** No committed secrets, credentials, tokens, provider keys, SQL leakage, prompt leakage, stack traces, or raw provider errors.
4. **Validation:** Verify backend request validation, required/optional fields, documented boundaries, and meaningful error responses.
5. **State machine:** Verify only the specified status transitions are accepted and rejected transitions do not mutate persisted state.
6. **RAG ingestion:** Verify searchable ticket knowledge is refreshed when relevant ticket content changes and obsolete knowledge does not remain authoritative.
7. **Grounding:** Verify no-match skips generation, answers use retrieved ticket context only, and returned source ticket IDs come from qualifying retrieved documents.
8. **Retrieval configuration:** Verify top-K and similarity threshold are configurable rather than hardcoded in business logic.
9. **Tests:** Check positive, negative, boundary, state-machine, persistence, retrieval, no-match, citation/source-integrity, re-ingestion, and hallucination coverage. Flag tautological or ineffective tests.
10. **Scope creep:** Flag unrelated features such as SLA management, attachments, email workflows, autonomous agents, brokers, schedulers, or additional infrastructure not required by the specification.
11. **Hallucinated APIs:** Verify imported classes, Spring APIs, model/vector-store APIs, configuration properties, and dependencies actually exist in this repository or its declared dependencies.

## Output

- Critical issues
- Major issues
- Minor issues
- Scope creep
- Specification gaps discovered in code
- Missing or weak tests
- Recommended fixes requiring human approval
