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
