const BASE_URL = '/api';

function token() {
  return localStorage.getItem('mindbridge_token');
}

function authHeaders() {
  const t = token();
  return t ? { 'Authorization': `Bearer ${t}`, 'Content-Type': 'application/json' } : { 'Content-Type': 'application/json' };
}

// --- Auth ---

export async function login(username, password) {
  const res = await fetch(`${BASE_URL}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  });
  if (!res.ok) throw new Error('Login failed');
  const data = await res.json();
  localStorage.setItem('mindbridge_token', data.token);
  localStorage.setItem('mindbridge_role', data.role);
  return data;
}

export async function register(username, password) {
  const res = await fetch(`${BASE_URL}/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  });
  if (!res.ok) throw new Error('Register failed');
  const data = await res.json();
  localStorage.setItem('mindbridge_token', data.token);
  localStorage.setItem('mindbridge_role', data.role);
  return data;
}

export function logout() {
  localStorage.removeItem('mindbridge_token');
  localStorage.removeItem('mindbridge_role');
}

export function getRole() {
  return localStorage.getItem('mindbridge_role') || 'USER';
}

export function isLoggedIn() {
  return !!token();
}

// --- Sessions ---

export async function getSessions() {
  const res = await fetch(`${BASE_URL}/sessions`, { headers: authHeaders() });
  if (!res.ok) throw new Error('Failed to fetch sessions');
  return res.json();
}

export async function createSession(title, model = 'deepseek') {
  const res = await fetch(`${BASE_URL}/sessions`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ title, model }),
  });
  if (!res.ok) throw new Error('Failed to create session');
  return res.json();
}

export async function deleteSession(id) {
  const res = await fetch(`${BASE_URL}/sessions/${id}`, {
    method: 'DELETE',
    headers: authHeaders(),
  });
  if (!res.ok) throw new Error('Failed to delete session');
  return res.json();
}

export async function getMessages(sessionId) {
  const res = await fetch(`${BASE_URL}/sessions/${sessionId}/messages`, { headers: authHeaders() });
  if (!res.ok) throw new Error('Failed to fetch messages');
  return res.json();
}

export function streamChat(sessionId, content, model = 'deepseek', { onToken, onDone, onError }) {
  fetch(`${BASE_URL}/chat/stream/${sessionId}`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ content, model }),
  })
    .then(async (response) => {
      if (!response.ok) throw new Error('Stream request failed');
      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = '';

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop() || '';
        for (const line of lines) {
          if (line.startsWith('data:')) {
            try {
              const data = JSON.parse(line.substring(5).trim());
              if (data.done) {
                onDone && onDone(data);
              } else if (data.token) {
                onToken && onToken(data.token);
              }
            } catch (e) { /* skip */ }
          }
        }
      }
    })
    .catch((err) => onError && onError(err));
}

// --- Knowledge ---

export async function uploadKnowledge(title, content, fileType = 'text') {
  const res = await fetch(`${BASE_URL}/knowledge/upload`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ title, content, fileType }),
  });
  if (!res.ok) throw new Error('Failed to upload knowledge');
  return res.json();
}

export async function getKnowledgeList() {
  const res = await fetch(`${BASE_URL}/knowledge/list`, { headers: authHeaders() });
  if (!res.ok) throw new Error('Failed to fetch knowledge');
  return res.json();
}

export async function deleteKnowledge(id) {
  const res = await fetch(`${BASE_URL}/knowledge/${id}`, {
    method: 'DELETE',
    headers: authHeaders(),
  });
  if (!res.ok) throw new Error('Failed to delete knowledge');
  return res.json();
}
