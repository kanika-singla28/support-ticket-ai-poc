# Support Ticket Management System — Data Model

## 1. Purpose

This document defines the relational and vector data model for the Support Ticket Management System.

PostgreSQL is used for both relational application data and PGVector storage.

Relational ticket data is authoritative. Vector data is derived from ticket data and is used only for semantic retrieval.

## 2. Main Entities

The application contains the following logical entities:

- SupportTicket
- SupportTicketComment
- Assignee
- Vector Knowledge Document

The initial standalone implementation already contains the `SupportTicket` entity. Additional entities and fields will be implemented according to this specification.

## 3. SupportTicket

A support ticket represents a user-reported issue.

Required fields:

| Field | Type | Required | Description |
|---|---|---|---|
| id | Long | Yes | Database-generated primary key |
| title | String | Yes | Short ticket title |
| description | String | Yes | Detailed problem description |
| priority | Enum | Yes | LOW, MEDIUM, HIGH |
| status | Enum | Yes | Ticket lifecycle status |
| category | String | Yes | Ticket category |
| assignee | Assignee | No | Current ticket owner |
| resolutionInformation | String | No | Resolution details |
| createdAt | Timestamp | Yes | Ticket creation time |
| updatedAt | Timestamp | Yes | Last modification time |

Ticket status defaults to `OPEN`.

Priority and status must be represented as controlled values rather than arbitrary user-entered strings in the final implementation.

## 4. Ticket Status

Supported values:

- OPEN
- IN_PROGRESS
- RESOLVED
- CLOSED
- CANCELLED

Status changes must follow the state machine defined in `state-machine.md`.

## 5. Ticket Priority

Supported values:

- LOW
- MEDIUM
- HIGH

Priority is also included in vector metadata so AI retrieval results can be associated with the original ticket priority.

## 6. SupportTicketComment

A support ticket can contain multiple comments.

Comments form part of the ticket history and must be included in the RAG knowledge representation.

Fields:

| Field | Type | Required | Description |
|---|---|---|---|
| id | Long | Yes | Database-generated primary key |
| ticketId | Long | Yes | Parent support ticket |
| content | String | Yes | Comment text |
| createdAt | Timestamp | Yes | Comment creation time |

A comment belongs to exactly one support ticket.

Adding a comment changes the searchable knowledge for the ticket and therefore triggers re-ingestion of that ticket.

## 7. Assignee

The assignment requires the ability to update a ticket assignee.

For this standalone POC, an assignee is represented as application data associated with a ticket.

Minimum assignee information:

| Field | Type | Required | Description |
|---|---|---|---|
| id | Long | Yes | Assignee identifier |
| name | String | Yes | Display name |

A ticket may have no assignee.

Assignee information is also included in RAG metadata as required by the assignment.

Authentication, user provisioning and enterprise identity management are outside the scope of this POC.

## 8. Resolution Information

A ticket may contain resolution information describing how the issue was resolved.

Resolution information is optional while a ticket is active.

It becomes important RAG knowledge for questions such as:

- What was the resolution for a previous ticket?
- How were similar issues resolved?

Resolution information must therefore be included when generating searchable knowledge documents.

Changes to resolution information trigger ticket re-ingestion.

## 9. Entity Relationships

The primary relational relationships are:

SupportTicket
→ may have one Assignee

SupportTicket
→ has zero or more SupportTicketComments

SupportTicket
→ produces one or more derived vector knowledge documents

The vector documents are derived search data and do not replace relational entities.

Deleting or replacing vector documents must never delete the authoritative relational ticket.

## 10. Vector Knowledge Document

Ticket information is transformed into searchable documents before embedding.

Each vector document contains:

### Content

Content may be derived from:

- Ticket title
- Description
- Comments
- Resolution information

### Required Metadata

Each vector document includes:

| Metadata | Purpose |
|---|---|
| ticketId | Identifies the authoritative source ticket |
| status | Ticket lifecycle state |
| priority | Ticket priority |
| assignee | Ticket assignee |
| category | Ticket category |

Metadata supports source attribution and may also support retrieval filtering.

The LLM must not invent source ticket IDs. Sources returned by the AI API are derived from this metadata.

## 11. Persistence Constraints

The backend must enforce data integrity independently of the frontend.

Required constraints include:

- Ticket title must not be blank.
- Ticket description must not be blank.
- Priority must contain a supported value.
- Status must contain a supported value.
- Category must not be blank.
- Comment content must not be blank.
- A comment must reference an existing ticket.
- An assigned assignee must reference valid assignee data.

Field-length limits will be defined in the API contract and backend validation rules.

The database provides persistence constraints while Spring validation provides meaningful API-level validation errors.

## 12. Vector Data Lifecycle

Vector data is derived from authoritative ticket data.

When searchable ticket information changes:

1. The relational change is persisted.
2. Existing vector documents belonging to the ticket are identified.
3. Stale vector documents are removed or replaced.
4. Current ticket information is converted into knowledge documents.
5. Documents are chunked according to `rag-ingestion.md`.
6. New embeddings are generated.
7. Updated documents and metadata are stored in PGVector.

This prevents retrieval from using stale ticket information.

Vector-document identity must support replacement by ticket so that re-ingestion does not continuously create duplicate stale knowledge.

## 13. Source-of-Truth Rule

PostgreSQL relational ticket data is the authoritative source of truth.

PGVector contains a derived semantic-search representation.

Therefore:

- Ticket CRUD operates on relational entities.
- The UI reads authoritative ticket information through ticket APIs.
- RAG retrieval operates on vector documents.
- Vector metadata points back to authoritative ticket IDs.
- Vector data must never independently modify ticket state.
- The LLM must never modify authoritative ticket data.
- If vector data becomes stale or inconsistent, relational ticket data takes precedence.

This separation keeps deterministic application state independent from probabilistic AI behavior.

## 14. Data Model Decision Summary

| Concern | Decision |
|---|---|
| Relational database | PostgreSQL |
| Vector store | PGVector |
| Ticket primary key | Database-generated Long |
| Ticket status | Controlled enum |
| Ticket priority | LOW, MEDIUM, HIGH |
| Comments | Separate relational entity linked to ticket |
| Assignee | Optional ticket relationship |
| Resolution information | Optional ticket knowledge field |
| Vector content | Derived from ticket history |
| Vector metadata | ticketId, status, priority, assignee, category |
| Embedding dimension | 768 |
| Authoritative data | Relational ticket data |
| Vector data | Derived and replaceable |
| Re-ingestion | Required when searchable ticket knowledge changes |

## 15. Implementation Boundary

The data model specification describes the target implementation.

Existing prototype classes may not yet match this model exactly.

During implementation:

- String status will be replaced with a controlled status enum.
- String priority will be replaced with a controlled priority enum.
- Comment persistence will be added.
- Assignee support will be added.
- Resolution information will be added where required.
- Update timestamps will be maintained.
- Vector-document creation and replacement will be implemented.

These changes must be made through the remaining specification, implementation, testing, review and fix stages rather than treating the current prototype as the final design.

## 16. Scope Control

The assignment does not require a complete enterprise user-management or ticketing platform.

The data model therefore does not introduce unrelated concepts such as:

- SLA management
- Departments
- Attachments
- Customer organizations
- Notification preferences
- Roles and permissions
- Tags
- Severity separate from priority

Additional fields or entities should only be introduced when justified by an approved requirement or specification change.
