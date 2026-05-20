// Central place for talking to the backend.
// The base URL comes from an env var so the deployed frontend can point at the
// deployed backend. Falls back to localhost for development.
const BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// Read the saved JWT (set at login) and attach it to every request.
function authHeaders() {
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

async function request(path, options = {}) {
  const res = await fetch(`${BASE}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders(),
      ...(options.headers || {}),
    },
  });

  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    throw new Error(data.message || `Request failed (${res.status})`);
  }
  return data;
}

export const api = {
  register: (body) => request('/api/auth/register', { method: 'POST', body: JSON.stringify(body) }),
  login:    (body) => request('/api/auth/login',    { method: 'POST', body: JSON.stringify(body) }),
  account:  ()     => request('/api/account'),
  statement:()     => request('/api/account/statement'),
  deposit:  (amount) => request('/api/account/deposit',  { method: 'POST', body: JSON.stringify({ amount }) }),
  withdraw: (amount) => request('/api/account/withdraw', { method: 'POST', body: JSON.stringify({ amount }) }),
  transfer: (toAccount, amount) => request('/api/account/transfer', { method: 'POST', body: JSON.stringify({ toAccount, amount }) }),
};
