# Support Ticket AI — RAG Proof of Concept

A standalone support-ticket management proof of concept built with Spring Boot, PostgreSQL/PGVector, Spring AI, Ollama, and React.

The application combines support-ticket lifecycle management with Retrieval-Augmented Generation (RAG). Ticket information is stored relationally in PostgreSQL and indexed into PGVector so users can ask natural-language questions about existing support tickets.

## Features

### Support Ticket Management

The application supports:

- Create support tickets
- List all tickets
- Search tickets by title or description
- Filter tickets by status
- Retrieve ticket details
- Update ticket information
- Assign tickets
- Add resolution information
- Add comments
- Transition tickets through controlled lifecycle rules

Supported priorities:

- `LOW`
- `MEDIUM`
- `HIGH`

Supported statuses:

- `OPEN`
- `IN_PROGRESS`
- `RESOLVED`
- `CLOSED`
- `CANCELLED`

## Ticket State Machine

Allowed transitions are:

```text
OPEN
 ├──> IN_PROGRESS
 └──> CANCELLED

IN_PROGRESS
 ├──> RESOLVED
 └──> CANCELLED

RESOLVED
 └──> CLOSED

CLOSED
 └──> terminal

CANCELLED
 └──> terminal
```

Invalid status transitions return HTTP `409 Conflict` and do not modify the ticket.

## RAG Pipeline

Support ticket knowledge is indexed in PostgreSQL using PGVector.

The indexed representation includes:

- Ticket ID
- Title
- Description
- Priority
- Status
- Category
- Assignee
- Resolution information
- Comments

When RAG-relevant ticket information changes, the ticket is re-indexed so that the vector representation remains synchronized with the latest ticket data.

The implementation maintains one current vector representation per ticket, preventing stale or duplicate vectors from remaining searchable.

## Ask Support Ticket AI

The application provides the following RAG endpoint:

`POST /api/ai/ask`

Example request:

    {
      "question": "How was the checkout payment timeout resolved?"
    }

The RAG flow is:

    User Question
         |
         v
    Embedding Model
         |
         v
    PGVector Similarity Search
         |
         v
    Relevant Ticket Documents
         |
         v
    Grounded Prompt
         |
         v
    Ollama Chat Model
         |
         v
    Answer + Ticket Sources

The assistant is instructed to answer only from retrieved support-ticket information.

If no relevant ticket is found, the API returns a no-relevant-ticket response.

If a relevant ticket is retrieved but does not contain enough evidence to answer the question, the assistant returns an insufficient-information response rather than inventing information.

## Technology Stack

### Backend

- Java 21
- Spring Boot 3.5.8
- Spring AI 1.1.2
- Spring Data JPA
- PostgreSQL
- PGVector
- Gradle 8.9
- Jakarta Bean Validation
- JUnit 5
- Mockito

### AI

- Ollama
- `nomic-embed-text` embedding model
- `llama3.2:1b` chat model
- Spring AI PGVector integration
- HNSW vector index
- Cosine distance
- 768-dimensional embeddings

### Frontend

- React
- Vite
- JavaScript
- CSS

## Project Structure

    support-ticket-poc-standalone/
    ├── backend/
    │   ├── src/main/java/
    │   ├── src/main/resources/
    │   └── src/test/java/
    ├── poc-ui/
    │   └── src/
    ├── docs/
    │   ├── ai-mistakes.md
    │   ├── implementation-plan.md
    │   └── prompt-history.md
    ├── spec/
    ├── rules/
    ├── skills/
    └── README.md

## Running the Backend

Java 21 or newer is required.

    cd backend

    export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
    export PATH="$JAVA_HOME/bin:$PATH"

    ./gradlew bootRun --no-daemon

The backend runs at:

`http://localhost:8080/api`

Database and Ollama configuration can be supplied using the environment variables defined in `application.properties`.

Do not commit real credentials to the repository.

## Running the Frontend

In another terminal:

    cd poc-ui
    npm install
    npm run dev

The Vite development server runs at:

`http://localhost:5173`

During development, Vite proxies `/api` requests to the Spring Boot backend on port `8080`.

## Main API Endpoints

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/tickets` | Create ticket |
| `GET` | `/api/tickets` | List, search, and filter tickets |
| `GET` | `/api/tickets/{ticketId}` | Get ticket details |
| `PATCH` | `/api/tickets/{ticketId}` | Update ticket |
| `POST` | `/api/tickets/{ticketId}/comments` | Add comment |
| `PATCH` | `/api/tickets/{ticketId}/status` | Change ticket status |
| `POST` | `/api/ai/ask` | Ask a RAG-grounded ticket question |

Example filtering:

    GET /api/tickets?search=checkout
    GET /api/tickets?status=IN_PROGRESS
    GET /api/tickets?search=checkout&status=IN_PROGRESS

## Automated Tests

Run all backend tests with:

    cd backend

    export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
    export PATH="$JAVA_HOME/bin:$PATH"

    ./gradlew clean test --no-daemon

The current automated test suite contains 20 tests:

    TicketStateMachineTest          11
    SupportTicketServiceTest         4
    SupportTicketControllerTest      5
                                   ---
    Total                           20

Coverage includes:

- Ticket state-machine rules
- Valid and invalid status transitions
- Ticket update behavior
- Comment behavior
- Ticket knowledge refresh interactions
- Prevention of persistence and re-indexing after rejected transitions
- Request validation
- Unsupported priority validation
- Ticket-not-found HTTP handling
- Invalid status-transition HTTP handling
- Invalid status-value validation

## Frontend Production Build

Run:

    cd poc-ui
    npm install
    npm run build
