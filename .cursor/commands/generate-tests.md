# Generate tests

Generate tests for the requested behaviour. Do not implement product features. Do not change production code to make tests pass.

## Process

1. Read the relevant spec first: `spec/requirements.md` plus `api-contract.md` / `state-machine.md` / `rag-ingestion.md` / `rag-api-contract.md` / `test-strategy.md` / `evaluation-strategy.md` as applicable.
2. List requirement and acceptance IDs in scope.
3. Split **deterministic** (exact assert) vs **probabilistic** (retrieval/generation eval).
4. Draft tests that map 1:1 to those IDs. Follow existing package layout under `src/test/java` and Spring Boot / JUnit 5 style. Do not enable Gradle tests unless the user is implementing.

## Required cases

- Positive path
- Negative / authorization / not-found
- Boundary (string max and max+1; threshold below/equal/above once comparison rule is chosen)
- State-machine: every allowed edge and rejected cell; terminal `TERMINAL_TICKET_READ_ONLY`
- Persistence + `PENDING` in same transaction; processing not before commit; rollback
- RAG: top-K, threshold, no-match (zero chat calls), citation subset, stale replacement

## Constraints

- Use test doubles for embedding/chat in deterministic tests.
- No credentials in tests.
- Do not invent APIs the production code does not have.
- If production behaviour is unspecified, stop and list questions instead of guessing.

## Output

- IDs covered
- Files to add/update
- Deterministic vs eval tests
- Gaps that still need human input
