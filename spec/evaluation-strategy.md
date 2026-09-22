# Support Ticket RAG — Evaluation Strategy

## 1. Purpose

This document defines how retrieval quality and AI answer quality are evaluated.

RAG evaluation is separated into two stages:

1. Retrieval evaluation — did the system retrieve the correct ticket evidence?
2. Generation evaluation — did the assistant answer only from that evidence?

This distinction is important because a grounded model cannot produce a correct ticket-specific answer when retrieval provides the wrong evidence.

## 2. Evaluation Dataset

A small deterministic evaluation dataset must be created for the POC.

The dataset contains support tickets with known:

- titles
- descriptions
- priorities
- statuses
- categories
- assignees
- comments
- resolution information

Evaluation questions are paired with expected relevant ticket IDs or an expected no-match result.

The dataset must include both positive and negative cases.

## 3. Retrieval Evaluation

For each evaluation question, semantic retrieval is executed without relying on the generated LLM answer.

The retrieved ticket IDs are compared with the expected relevant ticket IDs.

Evaluation should verify:

- expected ticket appears in retrieved results
- unrelated tickets are not treated as supporting evidence
- source metadata contains the correct ticket ID
- duplicate chunks do not create duplicate logical sources
- no-match questions do not pass the configured similarity threshold

## 4. Representative Retrieval Cases

The evaluation dataset should include questions such as:

| Question | Expected Behaviour |
|---|---|
| Have we seen payment failures before? | Retrieve relevant payment-failure ticket(s) |
| What was the resolution for ticket TKT-1001? | Retrieve TKT-1001 when present |
| What are common causes of shipment tracking issues? | Retrieve relevant shipment tickets |
| Show me similar resolved tickets. | Retrieve semantically related resolved-ticket evidence |
| Which high-priority tickets are related to payment? | Retrieve relevant HIGH-priority payment ticket evidence |
| What tickets discuss an unrelated topic absent from the dataset? | No relevant tickets |

Exact expected ticket IDs are defined by the test fixture data rather than invented in this specification.

## 5. Retrieval Parameter Tuning

The initial retrieval defaults are:

- Top-K = 5
- similarity threshold = 0.75

These are starting values, not guaranteed optimal values.

Evaluation must be used to determine whether these defaults:

- miss relevant tickets
- retrieve excessive unrelated tickets
- produce acceptable evidence for generation

Any tuning decision must be documented.

Retrieval parameters remain externally configurable after tuning.

## 6. Grounded Answer Evaluation

For questions with relevant retrieved evidence, the generated answer is evaluated against the retrieved context.

The answer passes grounding review when:

- factual support exists in retrieved ticket content
- ticket-specific causes are supported
- ticket-specific resolutions are supported
- the answer does not introduce unsupported ticket facts
- cited ticket IDs originate from retrieved metadata
- the answer does not rely on unrelated general knowledge

A fluent answer is not considered correct if it is unsupported by retrieved evidence.

## 7. Citation Evaluation

For every answer where `relevantTicketsFound` is true:

- `sources` must not be empty
- every source ticket ID must originate from retrieval metadata
- duplicate source ticket IDs must be removed
- cited tickets must have contributed relevant evidence

The generated model text is not used as the authoritative source of citation IDs.

## 8. No-Match Evaluation

The evaluation suite must include questions for which no relevant ticket exists.

Expected behaviour:

    relevantTicketsFound = false
    sources = []
    answer = explicit no-relevant-ticket response

The assistant must not provide a plausible support answer from general model knowledge.

This is a required hallucination guardrail.

## 9. Insufficient-Evidence Evaluation

A separate case must test retrieval that is semantically related but does not contain enough evidence to answer the requested detail.

For example, a retrieved ticket may mention a payment failure without documenting its root cause.

The assistant must not invent a root cause.

It should state that the available ticket evidence does not provide that information.

This distinguishes:

- no retrieved evidence
- retrieved but insufficient evidence
- retrieved and sufficient evidence

## 10. Re-Ingestion Evaluation

Tests must verify that ticket updates are reflected in semantic retrieval.

A representative test flow is:

    Create ticket
        ↓
    Ingest ticket
        ↓
    Verify original content is retrievable
        ↓
    Update searchable ticket information
        ↓
    Re-ingest ticket
        ↓
    Verify updated content is retrievable
        ↓
    Verify obsolete vector content is not retained as stale knowledge

Comment addition and status changes must also refresh relevant vector knowledge or metadata.

## 11. Hallucination Review

AI-generated answers must be reviewed specifically for unsupported claims.

The reusable project command:

`commands/review-rag-output.md`

will define the review procedure.

The review checks:

- whether every ticket-specific statement is supported
- whether causes and resolutions are grounded
- whether ticket IDs are real retrieved sources
- whether uncertainty is acknowledged
- whether the model answered despite insufficient evidence
- whether general knowledge leaked into a ticket-specific answer

## 12. AI Mistake Evidence

At least one meaningful AI mistake must be documented during development.

Valid examples include:

- incorrect generated code
- incorrect architectural suggestion
- unsupported RAG answer
- fabricated ticket detail
- incorrect source attribution
- retrieval configuration that produced demonstrably poor results

The mistake, why it was wrong, the engineering decision, and the correction must be recorded in:

`docs/ai-mistakes.md`

The already identified unsupported `CRITICAL` priority suggestion is retained as one example of human review of AI output.

## 13. Acceptance Criteria

RAG evaluation is successful when:

- known relevant questions retrieve expected ticket evidence
- no-match cases do not fabricate answers
- insufficient-evidence cases do not invent missing facts
- source IDs are derived from retrieval metadata
- duplicate source citations are eliminated
- updated ticket knowledge replaces stale vector knowledge
- retrieval parameters are configurable
- representative retrieval tuning is documented
- grounded-answer review finds no unsupported ticket-specific claims in the accepted evaluation cases
