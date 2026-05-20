import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api/client.js'

export default function Dashboard() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  const [account, setAccount] = useState(null);
  const [txns, setTxns] = useState([]);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const [depositAmt, setDepositAmt] = useState('');
  const [withdrawAmt, setWithdrawAmt] = useState('');
  const [toAccount, setToAccount] = useState('');
  const [transferAmt, setTransferAmt] = useState('');

  async function refresh() {
    try {
      const s = await api.statement();
      setAccount(s.account);
      setTxns(s.transactions);
    } catch (err) {
      setError(err.message);
      if (String(err.message).includes('401')) logout();
    }
  }

  useEffect(() => { refresh(); }, []);

  function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login');
  }

  async function doAction(fn) {
    setError(''); setBusy(true);
    try { await fn(); await refresh(); }
    catch (err) { setError(err.message); }
    finally { setBusy(false); }
  }

  const fmt = (n) => Number(n).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  const initials = (user.fullName || 'U').split(' ').map(s => s[0]).slice(0,2).join('').toUpperCase();

  return (
    <div className="dash">
      <header className="topbar">
        <div className="brand-row">
          <div className="brand-mark sm">A</div>
          Aegis Bank
        </div>
        <div className="topbar-right">
          <div className="who">
            <b>{user.fullName}</b>
            <span>{user.role === 'ROLE_ADMIN' ? 'Administrator' : 'Customer'}</span>
          </div>
          <button className="btn-ghost" onClick={logout}>Sign out</button>
        </div>
      </header>

      <main className="dash-main">
        <section className="balance-card">
          <div className="eyebrow"><span className="live"></span> Available balance</div>
          <div className="balance"><span className="cur">₹</span>{account ? fmt(account.balance) : '—'}</div>
          <div className="acct-no">Account&nbsp; <b>{account ? account.accountNumber : '••••••••'}</b></div>
        </section>

        {error && <div className="error wide">{error}</div>}

        <div className="action-grid">
          <div className="action-card">
            <div className="head">
              <div className="ic">↓</div>
              <div>
                <h3>Deposit</h3>
                <div className="sub">Add money to your account</div>
              </div>
            </div>
            <input type="number" min="0.01" step="0.01" placeholder="Amount (₹)"
                   value={depositAmt} onChange={e => setDepositAmt(e.target.value)} />
            <button className="btn-primary" disabled={busy || !depositAmt}
              onClick={() => doAction(async () => { await api.deposit(Number(depositAmt)); setDepositAmt(''); })}>
              Deposit funds
            </button>
          </div>

          <div className="action-card">
            <div className="head">
              <div className="ic">↑</div>
              <div>
                <h3>Withdraw</h3>
                <div className="sub">Take money out</div>
              </div>
            </div>
            <input type="number" min="0.01" step="0.01" placeholder="Amount (₹)"
                   value={withdrawAmt} onChange={e => setWithdrawAmt(e.target.value)} />
            <button className="btn-primary" disabled={busy || !withdrawAmt}
              onClick={() => doAction(async () => { await api.withdraw(Number(withdrawAmt)); setWithdrawAmt(''); })}>
              Withdraw funds
            </button>
          </div>

          <div className="action-card wide-card">
            <div className="head">
              <div className="ic">⇄</div>
              <div>
                <h3>Transfer</h3>
                <div className="sub">Send money to another Aegis account</div>
              </div>
            </div>
            <div className="transfer-row">
              <input placeholder="Recipient account (e.g. BANK10000002)"
                     value={toAccount} onChange={e => setToAccount(e.target.value)} />
              <input type="number" min="0.01" step="0.01" placeholder="Amount (₹)"
                     value={transferAmt} onChange={e => setTransferAmt(e.target.value)} />
              <button className="btn-primary" disabled={busy || !toAccount || !transferAmt}
                onClick={() => doAction(async () => { await api.transfer(toAccount, Number(transferAmt)); setToAccount(''); setTransferAmt(''); })}>
                Send money
              </button>
            </div>
          </div>
        </div>

        <section className="history">
          <div className="h-head">
            <h3>Transaction history</h3>
            <span className="count">{txns.length} {txns.length === 1 ? 'entry' : 'entries'}</span>
          </div>
          {txns.length === 0 ? (
            <div className="empty">
              <div className="big">No transactions yet</div>
              <div>Make a deposit or transfer to see it here.</div>
            </div>
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr><th>Date</th><th>Type</th><th>Counterparty</th><th className="right">Amount</th><th className="right">Balance</th></tr>
                </thead>
                <tbody>
                  {txns.map((t, i) => {
                    const credit = t.type === 'DEPOSIT' || t.type === 'TRANSFER_IN';
                    return (
                      <tr key={i}>
                        <td className="t-date">{new Date(t.timestamp).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' })}</td>
                        <td><span className={`badge ${credit ? 'in' : 'out'}`}>{t.type.replace('_', ' ')}</span></td>
                        <td>{t.counterparty || '—'}</td>
                        <td className={`right ${credit ? 'pos' : 'neg'}`}>{credit ? '+' : '−'} ₹{fmt(t.amount)}</td>
                        <td className="right">₹{fmt(t.balanceAfter)}</td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </main>
    </div>
  );
}
