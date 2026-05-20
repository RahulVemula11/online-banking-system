import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api/client.js'

export default function Login() {
  const [mode, setMode] = useState('login'); // 'login' | 'register'
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  async function submit(e) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = mode === 'login'
        ? await api.login({ username, password })
        : await api.register({ username, password, fullName });

      localStorage.setItem('token', res.token);
      localStorage.setItem('user', JSON.stringify(res));
      navigate('/dashboard');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-wrap">
      <div className="auth-card">
        <div className="brand">
          <div className="brand-mark">A</div>
          <div>
            <h1>Aegis Bank</h1>
            <p className="muted">Private &amp; Secure Banking</p>
          </div>
        </div>

        <div className="tabs">
          <button className={mode === 'login' ? 'tab active' : 'tab'} onClick={() => { setMode('login'); setError(''); }}>Sign in</button>
          <button className={mode === 'register' ? 'tab active' : 'tab'} onClick={() => { setMode('register'); setError(''); }}>Open account</button>
        </div>

        <form onSubmit={submit}>
          {mode === 'register' && (
            <label>Full name
              <input value={fullName} onChange={e => setFullName(e.target.value)} required placeholder="Jane Doe" />
            </label>
          )}
          <label>Username
            <input value={username} onChange={e => setUsername(e.target.value)} required placeholder="your username" autoComplete="username" />
          </label>
          <label>Password
            <input type="password" value={password} onChange={e => setPassword(e.target.value)} required placeholder="••••••••" autoComplete="current-password" />
          </label>

          {error && <div className="error">{error}</div>}

          <button className="btn-primary" disabled={loading}>
            {loading ? 'Please wait…' : (mode === 'login' ? 'Sign in securely' : 'Create my account')}
          </button>
        </form>

        <div className="demo-hint">
          <strong>Demo logins</strong><br/>
          alice / password123 &nbsp;·&nbsp; bob / password123
        </div>
      </div>
    </div>
  );
}
