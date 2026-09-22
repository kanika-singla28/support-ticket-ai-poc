import { useEffect, useState } from 'react'

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

  const [tickets, setTickets] = useState([])
  const [ticketsLoading, setTicketsLoading] = useState(false)
  const [ticketsError, setTicketsError] = useState('')
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [selectedTicket, setSelectedTicket] = useState(null)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState('')

  const [updateForm, setUpdateForm] = useState({
    title: '',
    description: '',
    priority: '',
    category: '',
    assignee: '',
    resolutionInformation: ''
  })
  const [commentText, setCommentText] = useState('')
  const [nextStatus, setNextStatus] = useState('')
  const [lifecycleLoading, setLifecycleLoading] = useState(false)
  const [lifecycleError, setLifecycleError] = useState('')
  const [lifecycleSuccess, setLifecycleSuccess] = useState('')

  const [question, setQuestion] = useState('')
  const [aiResponse, setAiResponse] = useState(null)
  const [asking, setAsking] = useState(false)
  const [askError, setAskError] = useState('')

  const loadTickets = async () => {
    setTicketsLoading(true)
    setTicketsError('')

    try {
      const params = new URLSearchParams()

      if (search.trim()) {
        params.set('search', search.trim())
      }

      if (statusFilter) {
        params.set('status', statusFilter)
      }

      const query = params.toString()
      const response = await fetch(`/api/tickets${query ? `?${query}` : ''}`)
      const result = await response.json()

      if (!response.ok) {
        throw new Error(
          result?.errors?.join(', ') || `Request failed: ${response.status}`
        )
      }

      setTickets(result.data || [])
    } catch (error) {
      setTicketsError(error.message)
    } finally {
      setTicketsLoading(false)
    }
  }

  const loadTicketDetails = async (ticketId) => {
    setDetailLoading(true)
    setDetailError('')

    try {
      const response = await fetch(`/api/tickets/${ticketId}`)
      const result = await response.json()

      if (!response.ok) {
        throw new Error(
          result?.errors?.join(', ') || `Request failed: ${response.status}`
        )
      }

      setSelectedTicket(result.data)

      setUpdateForm({
        title: result.data.title || '',
        description: result.data.description || '',
        priority: result.data.priority || '',
        category: result.data.category || '',
        assignee: result.data.assignee || '',
        resolutionInformation: result.data.resolutionInformation || ''
      })

      setNextStatus(result.data.status || '')
    } catch (error) {
      setDetailError(error.message)
    } finally {
      setDetailLoading(false)
    }
  }

  useEffect(() => {
    loadTickets()
  }, [])

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
      await loadTickets()

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

  const updateTicket = async (event) => {
    event.preventDefault()
    setLifecycleLoading(true)
    setLifecycleError('')
    setLifecycleSuccess('')

    try {
      const response = await fetch(`/api/tickets/${selectedTicket.id}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(updateForm)
      })

      const result = await response.json()

      if (!response.ok) {
        throw new Error(
          result?.errors?.join(', ') || `Request failed: ${response.status}`
        )
      }

      setSelectedTicket(result.data)
      setLifecycleSuccess('Ticket updated successfully.')
      await loadTickets()
    } catch (error) {
      setLifecycleError(error.message)
    } finally {
      setLifecycleLoading(false)
    }
  }

  const addComment = async (event) => {
    event.preventDefault()

    if (!commentText.trim()) {
      return
    }

    setLifecycleLoading(true)
    setLifecycleError('')
    setLifecycleSuccess('')

    try {
      const response = await fetch(
        `/api/tickets/${selectedTicket.id}/comments`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            content: commentText.trim()
          })
        }
      )

      const result = await response.json()

      if (!response.ok) {
        throw new Error(
          result?.errors?.join(', ') || `Request failed: ${response.status}`
        )
      }

      setSelectedTicket(result.data)
      setCommentText('')
      setLifecycleSuccess('Comment added successfully.')
      await loadTickets()
    } catch (error) {
      setLifecycleError(error.message)
    } finally {
      setLifecycleLoading(false)
    }
  }

  const changeStatus = async (event) => {
    event.preventDefault()

    setLifecycleLoading(true)
    setLifecycleError('')
    setLifecycleSuccess('')

    try {
      const response = await fetch(
        `/api/tickets/${selectedTicket.id}/status`,
        {
          method: 'PATCH',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            status: nextStatus
          })
        }
      )

      const result = await response.json()

      if (!response.ok) {
        throw new Error(
          result?.errors?.join(', ') || `Request failed: ${response.status}`
        )
      }

      setSelectedTicket(result.data)
      setNextStatus(result.data.status)
      setLifecycleSuccess(
        `Status changed to ${result.data.status}.`
      )

      await loadTickets()
    } catch (error) {
      setLifecycleError(error.message)
    } finally {
      setLifecycleLoading(false)
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

        <section className="card ticket-management">
          <div className="card-header">
            <div>
              <span className="step">02</span>
              <h2>Support Tickets</h2>
            </div>
            <p>Search, filter and inspect support tickets.</p>
          </div>

          <form
            className="filter-form"
            onSubmit={(event) => {
              event.preventDefault()
              loadTickets()
            }}
          >
            <div className="row">
              <label>
                Search
                <input
                  type="text"
                  value={search}
                  placeholder="Search title or description..."
                  onChange={(e) => setSearch(e.target.value)}
                />
              </label>

              <label>
                Status
                <select
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value)}
                >
                  <option value="">ALL</option>
                  <option value="OPEN">OPEN</option>
                  <option value="IN_PROGRESS">IN_PROGRESS</option>
                  <option value="RESOLVED">RESOLVED</option>
                  <option value="CLOSED">CLOSED</option>
                  <option value="CANCELLED">CANCELLED</option>
                </select>
              </label>
            </div>

            <button type="submit" disabled={ticketsLoading}>
              {ticketsLoading ? 'Loading...' : 'Search Tickets'}
            </button>
          </form>

          {ticketsError && (
            <div className="error">{ticketsError}</div>
          )}

          {!ticketsLoading && !ticketsError && tickets.length === 0 && (
            <div className="empty-state">No tickets found.</div>
          )}

          <div className="ticket-list">
            {tickets.map((item) => (
              <button
                type="button"
                className="ticket-list-item"
                key={item.id}
                onClick={() => loadTicketDetails(item.id)}
              >
                <div className="ticket-list-title">
                  <strong>#{item.id} {item.title}</strong>
                  <span>{item.status}</span>
                </div>

                <div className="ticket-list-meta">
                  <span>{item.priority}</span>
                  <span>{item.category}</span>
                  <span>{item.assignee || 'Unassigned'}</span>
                </div>
              </button>
            ))}
          </div>

          {detailLoading && (
            <div className="loading-message">Loading ticket details...</div>
          )}

          {detailError && (
            <div className="error">{detailError}</div>
          )}

          {selectedTicket && !detailLoading && (
            <div className="ticket-detail">
              <div className="answer-title">
                Ticket #{selectedTicket.id}
              </div>

              <h3>{selectedTicket.title}</h3>

              <div className="detail-grid">
                <div>
                  <strong>Status</strong>
                  <span>{selectedTicket.status}</span>
                </div>
                <div>
                  <strong>Priority</strong>
                  <span>{selectedTicket.priority}</span>
                </div>
                <div>
                  <strong>Category</strong>
                  <span>{selectedTicket.category}</span>
                </div>
                <div>
                  <strong>Assignee</strong>
                  <span>{selectedTicket.assignee || 'Unassigned'}</span>
                </div>
              </div>

              <div className="detail-section">
                <strong>Description</strong>
                <p>{selectedTicket.description}</p>
              </div>

              <div className="detail-section">
                <strong>Resolution</strong>
                <p>
                  {selectedTicket.resolutionInformation || 'Not provided'}
                </p>
              </div>

              <div className="detail-section">
                <strong>Comments</strong>

                {selectedTicket.comments?.length ? (
                  <div className="comment-list">
                    {selectedTicket.comments.map((comment) => (
                      <div className="comment" key={comment.id}>
                        <p>{comment.content}</p>
                        <small>{comment.createdAt}</small>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p>No comments.</p>
                )}
              </div>

              <div className="timestamps">
                <span>Created: {selectedTicket.createdAt}</span>
                <span>Updated: {selectedTicket.updatedAt}</span>
              </div>

              <div className="lifecycle-actions">
                <h3>Manage Ticket</h3>

                {lifecycleError && (
                  <div className="error">{lifecycleError}</div>
                )}

                {lifecycleSuccess && (
                  <div className="success">{lifecycleSuccess}</div>
                )}

                <div className="lifecycle-section">
                  <h4>Update Ticket</h4>

                  <form onSubmit={updateTicket}>
                    <label>
                      Title
                      <input
                        type="text"
                        required
                        value={updateForm.title}
                        onChange={(e) =>
                          setUpdateForm({
                            ...updateForm,
                            title: e.target.value
                          })
                        }
                      />
                    </label>

                    <label>
                      Description
                      <textarea
                        required
                        rows="3"
                        value={updateForm.description}
                        onChange={(e) =>
                          setUpdateForm({
                            ...updateForm,
                            description: e.target.value
                          })
                        }
                      />
                    </label>

                    <div className="row">
                      <label>
                        Priority
                        <select
                          value={updateForm.priority}
                          onChange={(e) =>
                            setUpdateForm({
                              ...updateForm,
                              priority: e.target.value
                            })
                          }
                        >
                          <option value="LOW">LOW</option>
                          <option value="MEDIUM">MEDIUM</option>
                          <option value="HIGH">HIGH</option>
                        </select>
                      </label>

                      <label>
                        Category
                        <input
                          type="text"
                          value={updateForm.category}
                          onChange={(e) =>
                            setUpdateForm({
                              ...updateForm,
                              category: e.target.value
                            })
                          }
                        />
                      </label>
                    </div>

                    <label>
                      Assignee
                      <input
                        type="text"
                        value={updateForm.assignee}
                        placeholder="Unassigned"
                        onChange={(e) =>
                          setUpdateForm({
                            ...updateForm,
                            assignee: e.target.value
                          })
                        }
                      />
                    </label>

                    <label>
                      Resolution Information
                      <textarea
                        rows="3"
                        value={updateForm.resolutionInformation}
                        onChange={(e) =>
                          setUpdateForm({
                            ...updateForm,
                            resolutionInformation: e.target.value
                          })
                        }
                      />
                    </label>

                    <button
                      type="submit"
                      disabled={lifecycleLoading}
                    >
                      {lifecycleLoading ? 'Saving...' : 'Update Ticket'}
                    </button>
                  </form>
                </div>

                <div className="lifecycle-section">
                  <h4>Add Comment</h4>

                  <form onSubmit={addComment}>
                    <textarea
                      required
                      rows="3"
                      value={commentText}
                      placeholder="Add a support ticket comment..."
                      onChange={(e) => setCommentText(e.target.value)}
                    />

                    <button
                      type="submit"
                      disabled={lifecycleLoading || !commentText.trim()}
                    >
                      {lifecycleLoading ? 'Saving...' : 'Add Comment'}
                    </button>
                  </form>
                </div>

                <div className="lifecycle-section">
                  <h4>Change Status</h4>

                  <form onSubmit={changeStatus}>
                    <select
                      value={nextStatus}
                      onChange={(e) => setNextStatus(e.target.value)}
                    >
                      <option value="OPEN">OPEN</option>
                      <option value="IN_PROGRESS">IN_PROGRESS</option>
                      <option value="RESOLVED">RESOLVED</option>
                      <option value="CLOSED">CLOSED</option>
                      <option value="CANCELLED">CANCELLED</option>
                    </select>

                    <button
                      type="submit"
                      disabled={
                        lifecycleLoading ||
                        !nextStatus ||
                        nextStatus === selectedTicket.status
                      }
                    >
                      {lifecycleLoading ? 'Changing...' : 'Change Status'}
                    </button>
                  </form>
                </div>
              </div>
            </div>
          )}
        </section>

        <section className="card">
          <div className="card-header">
            <div>
              <span className="step">03</span>
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
        Spring Boot · PostgreSQL · PGVector · Ollama · Spring AI
      </footer>
    </div>
  )
}

export default App
