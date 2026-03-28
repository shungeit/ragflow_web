import request from './request'

export function listAssistants(params) {
  return request.get('/chat/assistants', { params })
}

export function listAgents(params) {
  return request.get('/chat/agents', { params })
}

export function createSession(data) {
  return request.post('/chat/sessions', data)
}

export function listSessions(params) {
  return request.get('/chat/sessions', { params })
}

export function updateSession(data) {
  return request.put('/chat/sessions', data)
}

export function deleteSessions(data) {
  return request.delete('/chat/sessions', { data })
}

export function sendMessage(data) {
  return request.post('/chat/messages', data)
}

/**
 * Streaming chat via fetch + ReadableStream (SSE format)
 * Returns an object with { reader, response } for manual control
 */
export async function streamChat(data) {
  const token = localStorage.getItem('user_token')
  // Determine endpoint based on type
  const isAgent = !!data.agent_id
  const endpoint = isAgent ? '/api/chat/completions/agent' : '/api/chat/completions'
  // Map to backend expected format
  const body = {
    chatId: data.assistant_id,
    agentId: data.agent_id,
    sessionId: data.session_id,
    question: data.messages?.[0]?.content || data.question || '',
    stream: true
  }

  const response = await fetch(endpoint, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify(body)
  })

  if (!response.ok) {
    throw new Error(`HTTP ${response.status}: ${response.statusText}`)
  }

  return response
}
