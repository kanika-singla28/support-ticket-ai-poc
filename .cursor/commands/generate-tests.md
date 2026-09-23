# Generate Tests

Generate tests for the requested behaviour. Do not implement product features or weaken production behaviour merely to make generated tests pass.

## Process

1. Read the relevant specifications first: `spec/requirements.md`, `spec/api-contract.md`, `spec/state-machine.md`, `spec/rag-ingestion.md`, `spec/rag-api-contract.md`, `spec/test-strategy.md`, and `spec/evaluation-strategy.md` as applicable.
2. Identify the requirement and acceptance criteria covered by the requested tests.
3. Separate deterministic tests from probabilistic RAG evaluation.
4. Follow the existing package layout under `backend/src/test/java` and the project's JUnit 5 / Spring Boot testing conventions.
5. Reuse existing test infrastructure and test doubles where appropriate.

## Required Cases

- Positive paths
- Validation and malformed-input cases
- Not-found cases
- Relevant boundary conditions from the API contract
- Ticket state machine: every allowed transition and representative/all specified invalid transitions
- Persistence: accepted mutations persist and rejected transitions do not mutate state
- Ticket search and status filtering
- Ticket knowledge creation and refresh/re-ingestion
- Retrieval using configured top-K and similarity threshold
- No-match response with no answer-generation call
- Source ticket IDs restricted to qualifying retrieved documents
- Unsupported or fabricated ticket-reference handling
- Stale/obsolete ticket knowledge replacement

## RAG Testing

- Use test doubles for embedding, vector retrieval, or chat generation when deterministic behaviour is being tested.
- Keep live-provider evaluation separate from deterministic unit/integration tests where practical.
- Do not treat a plausible generated answer as automatically correct.
- Validate claims against retrieved context.
- Never place provider credentials in test source or fixtures.

## Constraints

- Do not invent production APIs that do not exist.
- Do not hardcode secrets.
- Do not silently change unspecified behaviour.
- If a required behaviour is ambiguous, report the specification gap instead of guessing.

## Output

- Requirements/acceptance criteria covered
- Test files to add or update
- Deterministic tests
- RAG/evaluation tests
- Remaining specification or coverage gaps
