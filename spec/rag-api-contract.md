# Support Ticket RAG — API Contract

## 1. Purpose

This document defines the contract for natural-language question answering over support-ticket history.

The endpoint implements a single:

Retrieval → Context Construction → Generation → Grounded Response

flow.

It is not an autonomous agent and must not perform ticket mutations or external actions.

## 2. Endpoint

Endpoint:

`POST /api/ai/ask`

Request:

    {
      "question": "What caused previous payment failures?"
    }

The `question` field is required and must not be blank.

Blank or invalid requests return `400 Bad Request`.

## 3. Successful Response

A successful request returns `200 OK`.

Example:

    {
      "data": {
        "answer": "Previous payment failures were associated with gateway timeout issues.",
        "relevantTicketsFound": true,
        "sources": [
          {
            "ticketId": 1001
          }
        ]
      },
      "errors": null
    }

The answer must be derived only from retrieved support-ticket context.

## 4. Retrieval

The user's question is used to perform semantic similarity search against PGVector.

Retrieval uses externally configurable parameters:

- Top-K
- similarity threshold

The API must not hardcode these retrieval values.

Only documents returned by retrieval are eligible to become grounding context for the answer.

## 5. Grounding Rule

The language model receives the user's question together with retrieved ticket context.

The generation instruction must explicitly require the model to:

- answer only from supplied ticket context
- not use general knowledge to fill missing support information
- not invent ticket facts
- not invent causes or resolutions
- not invent ticket IDs
- state that relevant information was not found when the supplied context does not support an answer

Retrieved context is evidence, not merely a suggestion to the model.

## 6. Source Attribution

Every ticket cited in the API response must originate from retrieved vector-document metadata.

The application constructs source citations from retrieved `ticketId` metadata.

The LLM must not be treated as the authoritative source of ticket IDs.

Duplicate retrieved chunks from the same ticket must not result in duplicate ticket citations.

For example, if three retrieved chunks belong to ticket `1001`, the response source list contains ticket `1001` once.

## 7. No Relevant Tickets

When semantic retrieval returns no documents satisfying the configured retrieval criteria, the application must not invoke unconstrained support-domain answering.

The response must explicitly indicate that no relevant tickets were found.

Example:

    {
      "data": {
        "answer": "No relevant tickets were found for this question.",
        "relevantTicketsFound": false,
        "sources": []
      },
      "errors": null
    }

This behavior is required to prevent plausible but ungrounded answers.

## 8. Insufficient Retrieved Evidence

Retrieval success does not automatically mean the retrieved context contains enough evidence to answer every aspect of a question.

The generation prompt must instruct the model not to infer unsupported details from weak or incomplete context.

If retrieved tickets are related but do not support the requested conclusion, the answer must acknowledge that the available ticket information is insufficient rather than fabricate the missing information.

Sources may still identify the retrieved tickets when they genuinely contributed to the response.

## 9. Example Supported Questions

Representative questions include:

- Have we seen payment failures before?
- What was the resolution for ticket TKT-1001?
- What are the common causes of shipment tracking issues?
- Show me similar resolved tickets.
- Which high-priority tickets are related to payment?

The endpoint is intended for questions about stored support-ticket history.

## 10. Out-of-Scope Questions

Questions unrelated to support-ticket history must not be answered using general LLM knowledge.

If retrieval finds no relevant ticket evidence, the endpoint returns the defined no-relevant-tickets response.

Examples may include unrelated questions about:

- general world knowledge
- weather
- mathematics unrelated to ticket history
- unrelated programming questions

The RAG endpoint remains bounded to the support-ticket knowledge base.

## 11. Response Source Integrity

The application must maintain a traceable relationship:

    AI source
        ↓
    retrieved vector document
        ↓
    ticketId metadata
        ↓
    authoritative support ticket

A source ID must never be produced solely because it appears in generated model text.

Source attribution is application-controlled.

## 12. Failure Behaviour

Infrastructure failures are different from valid no-match results.

Examples include:

- embedding-model unavailable
- PGVector unavailable
- Ollama generation model unavailable
- unexpected retrieval failure

Such failures must not be represented as:

`No relevant tickets were found`

because that would incorrectly imply successful retrieval.

Infrastructure failures must produce meaningful application errors and be logged without exposing secrets.

## 13. Non-Agent Boundary

The AI endpoint performs one question-answering operation.

It does not independently:

- create tickets
- update tickets
- transition ticket status
- add comments
- assign users
- send notifications
- invoke unrelated external tools
- recursively plan additional actions

This preserves the assignment's required single retrieval → generate architecture.
