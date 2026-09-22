import { useState } from 'react'

function App() {
  const [ticket, setTicket] = useState({
    title: '',
    description: '',
    priority: 'HIGH',
    category: 'Technical'
  })

  const [createdTicket, setCreatedTicket] = useState(null)
  const [creating, setCreating] = useState(false)
  const [createError, setCreateError] = useState('')

  const [question, setQuestion] = useState('')
  const [aiResponse, setAiResponse] = useState(null)
  const [asking, setAsking] = useState(false)
  const [askError, setAskError] = useState('')

  const createTicket = async (event) => {
    event.preventDefault()
    setCreating(true)
    setCreateError('')
    setCreatedTicket(null)

    try {
      const response = await fetch('/api/tickets', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(ticket)
      })

      const result = await response.json()

      if (!response.ok) {
        throw new Error(
          result?.errors?.join(', ') || `Request failed: ${response.status}`
        )
      }

      setCreatedTicket(result.data)

      setTicket({
        title: '',
        description: '',
        priority: 'HIGH',
        category: 'Technical'
      })
    } catch (error) {
      setCreateError(error.message)
    } finally {
      setCreating(false)
    }
  }

  const askAi = async (event) => {
    event.preventDefault()
    setAsking(true)
    setAskError('')
    setAiResponse(null)

    try {
      const response = await fetch('/api/ai/ask', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          question
        })
      })

      const result = await response.json()

      if (!response.ok) {
        throw new Error(
          result?.errors?.join(', ') || `Request failed: ${response.status}`
        )
      }

      setAiResponse(result.data)
    } catch (error) {
      setAskError(error.message)
    } finally {
      setAsking(false)
    }
  }

  return (
    <div className="page">
      <header className="header">
        <div>
          <h1>Support Ticket AI</h1>
          <p>PGVector + Ollama RAG Proof of Concept</p>
        </div>

        <div className="status">
          <span className="status-dot"></span>
          Local POC
        </div>
      </header>

      <main className="container">
        <section className="card">
          <div className="card-header">
            <div>
              <span className="step">01</span>
              <h2>Create Support Ticket</h2>
            </div>
            <p>Create a ticket and index it into the RAG pipeline.</p>
          </div>

          <form onSubmit={createTicket}>
            <label>
              Title
              <input
                type="text"
                required
                value={ticket.title}
                placeholder="Example: PGVector connection issue"
                onChange={(e) =>
                  setTicket({ ...ticket, title: e.target.value })
                }
              />
            </label>

            <label>
              Description
              <textarea
                required
                rows="5"
                value={ticket.description}
                placeholder="Describe the support issue..."
                onChange={(e) =>
                  setTicket({ ...ticket, description: e.target.value })
                }
              />
            </label>

            <div className="row">
              <label>
                Priority
                <select
                  value={ticket.priority}
                  onChange={(e) =>
                    setTicket({ ...ticket, priority: e.target.value })
                  }
                >
                  <option value="LOW">LOW</option>
                  <option value="MEDIUM">MEDIUM</option>
                  <option value="HIGH">HIGH</option>
                  <option value="CRITICAL">CRITICAL</option>
                </select>
              </label>

              <label>
                Category
                <input
                  type="text"
                  required
                  value={ticket.category}
                  onChange={(e) =>
                    setTicket({ ...ticket, category: e.target.value })
                  }
                />
              </label>
            </div>

            <button type="submit" disabled={creating}>
              {creating ? 'Creating...' : 'Create & Index Ticket'}
            </button>
          </form>

          {createdTicket && (
            <div className="success">
              <strong>✓ Ticket created successfully</strong>

              <div className="ticket-result">
                <span>Ticket #{createdTicket.id}</span>
                <span>{createdTicket.priority}</span>
                <span>{createdTicket.status}</span>
                <span>{createdTicket.category}</span>
              </div>

              <strong>{createdTicket.title}</strong>
            </div>
          )}

          {createError && (
            <div className="error">{createError}</div>
          )}
        </section>

        <section className="card">
          <div className="card-header">
            <div>
              <span className="step">02</span>
              <h2>Ask Support Ticket AI</h2>
            </div>
            <p>Ask a natural-language question about indexed tickets.</p>
          </div>

          <form onSubmit={askAi}>
            <label>
              Question
              <textarea
                required
                rows="4"
                value={question}
                placeholder="Example: What ticket is related to PGVector RAG?"
                onChange={(e) => setQuestion(e.target.value)}
              />
            </label>

            <button type="submit" disabled={asking}>
              {asking ? 'Searching & Generating...' : 'Ask AI'}
            </button>
          </form>

          {aiResponse && (
            <div className="ai-answer">
              <div className="answer-title">AI Answer</div>

              <p>{aiResponse.answer}</p>

              <div className="retrieval-status">
                Relevant tickets found:
                <strong>
                  {aiResponse.relevantTicketsFound ? ' Yes' : ' No'}
                </strong>
              </div>

              {aiResponse.sources?.length > 0 && (
                <div className="sources">
                  <strong>Sources</strong>

                  {aiResponse.sources.map((source, index) => (
                    <span key={index}>
                      Ticket #{source.ticketId}
                    </span>
                  ))}
                </div>
              )}
            </div>
          )}

          {askError && (
            <div className="error">{askError}</div>
          )}
        </section>
      </main>

      <footer>
        Spring Boot · MySQL · Ollama · PostgreSQL PGVector · Spring AI
      </footer>
    </div>
  )
}

export default App
