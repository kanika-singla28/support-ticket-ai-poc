---
name: documentation
description: >-
  Produce project documentation that separates requirements, architecture,
  implementation, and evaluation; cites FR/RAG/NFR/AC IDs; records decisions,
  alternatives, and assumptions; never claims unimplemented work or secrets.
  Use when writing README, spec notes, ADRs, evaluation reports, or ATL/TL
  documentation for the Support Ticket POC.
---

# Project documentation

## When to use

Writing or updating docs for this repository (README, `spec/`, ADRs, eval reports, planning notes).

## Source of truth

Read before writing:

- Requirements and acceptance: `spec/requirements.md`
- Architecture: `spec/architecture.md`
- Behaviour: `spec/api-contract.md`, `spec/state-machine.md`, `spec/ui-flow.md`
- RAG: `spec/rag-ingestion.md`, `spec/rag-api-contract.md`, `spec/evaluation-strategy.md`
- Tests: `spec/test-strategy.md`
- Code and git status — only treat behaviour as implemented if it exists in source

## Separate fact types

Label sections so they are not mixed:

| Kind | Allowed content |
|---|---|
| Requirement | `FR-*`, `RAG-*`, `NFR-*`, `AC-*` and assignment obligations |
| Architecture | PostgreSQL persistence, PGVector retrieval, RAG flow, provider/model choices, rejected alternatives |
| Implementation | What the current code actually does |
| Evaluation | Measured retrieval/grounding results, dataset version, models used |

Do not present a spec as shipped. Do not present a prototype as production-ready.

## Required practices

- Cite requirement IDs when documenting a behaviour (`FR-017`, `RAG-006`, `AC-012`).
- Record architectural decisions with at least: decision, why, alternatives considered, consequences.
- List assumptions in an explicit **Assumptions** subsection. Do not hide them in prose.
- Record AI-assisted decisions accurately (what the model proposed, what a human approved, date/phase if known).
- Keep docs synchronized: if code diverges, update the implementation section or mark it **not implemented**.
- Never paste secrets, tokens, passwords, connection strings, or provider keys. Refer to env var **names** only.

## Output skeleton

```markdown
# <Title>

## Status
Specification | Implementation | Evaluation (pick; do not blur)

## Requirements referenced
- FR-... / RAG-... / AC-...

## Decisions
- Decision:
- Alternatives:
- Why:

## Assumptions
- ...

## Implementation facts
Only code-backed statements.

## Evaluation facts
Only measured or explicitly not-yet-run items.

## AI-assisted decisions
- Proposed:
- Human disposition: approved | rejected | deferred
```
