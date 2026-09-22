# Review RAG output

Review one assistant (`POST /api/ai/ask`) result against retrieved context and `spec/rag-api-contract.md` / `spec/evaluation-strategy.md`. Do not call extra tools or invent tickets.

Inputs (required): user question, retrieved chunks (ticket IDs + text + scores if known), configured threshold/top-K, HTTP payload (`answer`, `relevantTicketsFound`, `sources`), whether the chat model was invoked.

## Checks

- Ticket-specific claims entailed by retrieved passages only
- No general LLM knowledge used as ticket fact
- No fabricated ticket IDs
- Every citation ∈ retrieved qualifying ticket IDs
- If no chunk meets threshold: `relevantTicketsFound=false`, empty sources, **no** generation
- Irrelevant or stale retrieved tickets (text/metadata older than MySQL)
- Threshold/top-K misapplication

## Output (use this structure)

```markdown
Question:
Retrieved ticket IDs:
Configured top-K / threshold:
Chat model invoked: yes/no

Answer claims:
- ...

Supported claims:
- claim → ticketId / passage

Unsupported claims:
- claim → why (missing from context / contradiction / general knowledge)

Citation validation:
- Cited IDs:
- Retrieved qualifying IDs:
- Illegal citations:
- Missing required citations (if deterministic):

Grounding result: PASS | FAIL
No-match correctness: PASS | FAIL | N/A
Stale/irrelevant retrieval: ...

Recommended action:
- accept | reject answer | raise threshold | re-ingest ticket(s) | fix prompt/citation filter | investigate retrieval
```
