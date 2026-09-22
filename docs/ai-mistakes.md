# AI Mistakes and Engineering Review

## AI Mistake 1 — Unsupported CRITICAL Priority

### AI Suggestion

During data-model specification, the AI suggested adding a `CRITICAL` ticket priority because it is common in support-ticket systems.

### Why It Was Incorrect

The supplied assignment does not require a `CRITICAL` priority. It refers to high-priority tickets but does not define `CRITICAL` as an allowed value.

The suggestion was based on a general software convention rather than evidence from the project requirements.

### Engineering Decision

The suggestion was rejected.

The POC currently defines:

- LOW
- MEDIUM
- HIGH

This keeps the implementation aligned with the supplied requirements and avoids unnecessary scope expansion.

### Lesson

AI-generated architecture suggestions must be checked against the authoritative requirement before being accepted.

A plausible industry convention is not automatically a project requirement.

## AI Mistake 2 — Retrieved Comment Was Ignored During Generation

### Observed Behaviour

During RAG evaluation, the question:

`Did the customer confirm whether checkout worked after the payment gateway change?`

retrieved the correct support ticket and returned ticket ID 3 as the source.

The refreshed PGVector document contained the comment:

`Customer confirmed that checkout succeeds after the payment gateway retry configuration change.`

However, the generation model incorrectly answered that there was no comment section for the ticket.

### Diagnosis

The RAG pipeline was evaluated in stages.

The following components were verified independently:

- PostgreSQL contained the comment.
- Ticket re-ingestion completed.
- PGVector contained the refreshed comment.
- Only one current vector existed for the ticket.
- Semantic retrieval returned the correct ticket.
- Source attribution returned the correct ticket ID.
- The retrieved document text contained the comment.

The failure therefore occurred during grounded answer generation rather than ingestion or retrieval.

### Engineering Fix

The grounding prompt was strengthened by:

- separating system instructions from the user prompt
- explicitly identifying comments and resolution information as authoritative evidence
- instructing the model to read those sections
- prohibiting claims that information is missing when it exists in supplied context
- requiring direct answers to the specific question
- defining explicit insufficient-evidence behaviour

Retrieval parameters were not changed because retrieval had already selected the correct ticket.

### Verification

After the fix:

- resolution questions returned the stored resolution
- comment questions correctly used the stored comment
- unrelated questions returned a no-relevant-ticket response
- questions requesting unavailable facts returned an insufficient-evidence response
- no unsupported HTTP status code was invented

### Lesson

Successful retrieval does not guarantee a grounded generated answer.

RAG evaluation must test retrieval and generation separately so that generation failures are not incorrectly addressed by changing vector-search parameters.
