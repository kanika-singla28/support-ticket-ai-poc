# Support Ticket Management System — REST API Contract

## 1. Purpose

This document defines the REST API contract for deterministic support-ticket operations.

The AI question-answering endpoint is specified separately in `rag-api-contract.md`.

All validation and state-transition rules are enforced by the backend.

## 2. API Conventions

Base path: `/api`

Ticket resource: `/api/tickets`

The API uses JSON for request and response bodies.

Errors must provide meaningful messages that can be displayed by the frontend.

HTTP status codes:

| Status | Meaning |
|---|---|
| 200 | Successful read or update |
| 201 | Resource created |
| 400 | Invalid request or validation failure |
| 404 | Requested ticket does not exist |
| 409 | Operation conflicts with current ticket state |
| 500 | Unexpected server error |

## 3. Create Ticket

Endpoint: `POST /api/tickets`

### Request Fields

| Field | Required | Description |
|---|---|---|
| title | Yes | Ticket title |
| description | Yes | Detailed issue description |
| priority | Yes | LOW, MEDIUM, or HIGH |
| category | Yes | Ticket category |

Example request:

    {
      "title": "Payment fails during checkout",
      "description": "Customer receives a timeout while completing payment.",
      "priority": "HIGH",
      "category": "PAYMENT"
    }

The backend assigns the ticket ID, initial status `OPEN`, creation timestamp, and update timestamp.

Assignee and resolution information are optional and are not required when creating a ticket.

### Response

Successful creation returns `201 Created` and the created ticket.

Invalid input returns `400 Bad Request` with a meaningful validation error.

## 4. List, Search and Filter Tickets

Endpoint: `GET /api/tickets`

With no query parameters, the endpoint returns the available support tickets.

### Query Parameters

| Parameter | Required | Description |
|---|---|---|
| search | No | Keyword used to search ticket content |
| status | No | Filters tickets by status |

Examples:

    GET /api/tickets
    GET /api/tickets?search=payment
    GET /api/tickets?status=OPEN
    GET /api/tickets?search=payment&status=RESOLVED

The `status` parameter accepts:

- OPEN
- IN_PROGRESS
- RESOLVED
- CLOSED
- CANCELLED

Search and status filtering may be used together.

Keyword search is deterministic database search and is separate from semantic RAG retrieval.

### Response

Successful requests return `200 OK` with the matching tickets.

If no tickets match, the endpoint returns `200 OK` with an empty result rather than treating the absence of matches as an error.

An invalid status value returns `400 Bad Request`.

## 5. View Ticket Details

Endpoint: `GET /api/tickets/{ticketId}`

The endpoint returns the complete ticket information required by the UI.

The response includes:

- ID
- title
- description
- priority
- status
- category
- assignee
- resolution information
- comments
- creation timestamp
- update timestamp

### Response

If the ticket exists, return `200 OK`.

If the ticket does not exist, return `404 Not Found` with a meaningful error message.

## 6. Update Ticket

Endpoint: `PATCH /api/tickets/{ticketId}`

This endpoint updates editable ticket fields.

Supported fields are:

- title
- description
- priority
- assignee
- resolutionInformation

`resolutionInformation` stores resolution notes used as part of the ticket's RAG knowledge when present.

Example request:

    {
      "title": "Payment timeout during checkout",
      "priority": "HIGH",
      "assigneeId": 2
    }

Only fields supplied in the request are updated.

Ticket status is not changed through this endpoint. Status transitions use the dedicated status-transition endpoint defined separately.

### Re-Ingestion Requirement

If an update changes information used by the RAG knowledge representation, the ticket must be re-ingested so its vector representation does not become stale.

### Response

Successful update returns `200 OK` with the updated ticket.

Invalid input returns `400 Bad Request`.

If the ticket does not exist, return `404 Not Found`.

If a supplied assignee is invalid, the backend rejects the update with a meaningful client error.

## 7. Add Comment

Endpoint: `POST /api/tickets/{ticketId}/comments`

Request:

    {
      "content": "Customer confirmed that the retry succeeded."
    }

The comment content must not be blank.

A successful comment becomes part of the authoritative ticket history.

Because comments are included in the RAG knowledge representation, adding a comment must trigger re-ingestion of the affected ticket.

### Response

Successful creation returns `201 Created` with the created comment.

Blank or invalid comment content returns `400 Bad Request`.

If the ticket does not exist, return `404 Not Found`.

## 8. Transition Ticket Status

Endpoint: `PATCH /api/tickets/{ticketId}/status`

Request:

    {
      "status": "IN_PROGRESS"
    }

The backend validates the requested transition against the ticket state machine.

Allowed transitions are:

    OPEN -> IN_PROGRESS
    IN_PROGRESS -> RESOLVED
    RESOLVED -> CLOSED
    OPEN -> CANCELLED
    IN_PROGRESS -> CANCELLED

All other transitions are rejected.

The frontend must not be relied upon to enforce these rules.

### Re-Ingestion Requirement

A successful status change must refresh the ticket's vector representation because `status` is required RAG metadata.

When resolution or closure changes searchable ticket knowledge, the refreshed representation must contain the latest authoritative ticket information.

### Response

Successful transition returns `200 OK` with the updated ticket.

An unsupported status value returns `400 Bad Request`.

A transition that uses a valid status value but is not allowed from the ticket's current state returns `409 Conflict`.

If the ticket does not exist, return `404 Not Found`.

## 9. Validation Rules

Backend validation is authoritative.

At minimum:

- title must not be blank
- description must not be blank
- category must not be blank
- priority must be a supported value
- status must be a supported value
- comment content must not be blank
- referenced tickets must exist
- supplied assignee references must be valid

Validation failures return `400 Bad Request` unless another status is explicitly defined by this contract.

## 10. Error Response

Errors must use a consistent JSON structure so the frontend can display meaningful feedback.

Example:

    {
      "message": "Invalid ticket status transition",
      "errors": [
        "Transition from CLOSED to OPEN is not allowed"
      ]
    }

Error responses must not expose stack traces, database credentials, model configuration, or other secrets.

## 11. RAG Synchronization Boundary

Ticket APIs operate on authoritative relational data.

Operations that change searchable ticket knowledge must refresh the derived vector representation.

This includes changes to:

- title
- description
- priority
- assignee
- category when applicable
- resolution information
- comments
- status

The vector store remains derived data and is never the authoritative source for ticket state.

Detailed ingestion and refresh behavior is defined in `rag-ingestion.md`.

## 12. AI API Boundary

Natural-language question answering is specified separately from deterministic ticket CRUD.

The AI endpoint is:

`POST /api/ai/ask`

Its request, retrieval, grounding, citation and no-match behavior are defined in `rag-api-contract.md`.

The AI endpoint performs a single retrieval-and-generation flow.

It does not create, update, transition or otherwise mutate support tickets.
