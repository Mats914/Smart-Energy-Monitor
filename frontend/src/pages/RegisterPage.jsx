import { useState } from 'react';
import { Zap } from 'lucide-react';

export default function RegisterPage({ onRegister, onSwitchToLogin, error, loading }) {
  const [form, setForm] = useState({ username: '', email: '', password: '' });

  const handleSubmit = e => {
    e.preventDefault();
    onRegister(form.username, form.email, form.password);
  };

  const field = (key, type = 'text', placeholder) => (
    <input
      style={styles.input}
      type={type}
      placeholder={placeholder}
      value={form[key]}
      onChange={e => setForm(f => ({ ...f, [key]: e.target.value }))}
      required
    />
  );

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        <div style={styles.logo}>
          <div style={styles.logoIcon}><Zap size={20} color="#f59e0b" /></div>
          <span style={styles.logoText}>SmartGrid Monitor</span>
        </div>
        <h1 style={styles.title}>Create account</h1>
        <p style={styles.sub}>Start monitoring your energy consumption</p>

        <form onSubmit={handleSubmit} style={styles.form}>
          {field('username', 'text', 'Username')}
          {field('email', 'email', 'Email')}
          {field('password', 'password', 'Password (min 6 chars)')}
          {error && <p style={styles.error}>{error}</p>}
          <button style={styles.btn} disabled={loading}>
            {loading ? 'Creating account...' : 'Create account'}
          </button>
        </form>

        <p style={styles.switchText}>
          Already have an account?{' '}
          <span style={styles.link} onClick={onSwitchToLogin}>Sign in</span>
        </p>
      </div>
    </div>
  );
}

const styles = {
  page: { minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#0f172a' },
  card: { background: '#1e293b', borderRadius: 16, padding: '2.5rem', width: '100%', maxWidth: 400, border: '1px solid #334155' },
  logo: { display: 'flex', alignItems: 'center', gap: 10, marginBottom: '2rem' },
  logoIcon: { width: 36, height: 36, background: '#0f172a', borderRadius: 8, display: 'flex', alignItems: 'center', justifyContent: 'center' },
  logoText: { fontWeight: 700, fontSize: 16, color: '#f1f5f9' },
  title: { fontSize: 24, fontWeight: 700, color: '#f1f5f9', marginBottom: 8 },
  sub: { fontSize: 14, color: '#94a3b8', marginBottom: '2rem' },
  form: { display: 'flex', flexDirection: 'column', gap: 12 },
  input: { padding: '10px 14px', borderRadius: 8, border: '1px solid #334155', background: '#0f172a', color: '#f1f5f9', fontSize: 14, outline: 'none' },
  error: { color: '#f87171', fontSize: 13, margin: 0 },
  btn: { padding: 11, borderRadius: 8, background: '#6366f1', color: '#fff', border: 'none', fontWeight: 600, fontSize: 14, cursor: 'pointer', marginTop: 4 },
  switchText: { textAlign: 'center', fontSize: 13, color: '#64748b', marginTop: '1.5rem' },
  link: { color: '#818cf8', cursor: 'pointer' },
};
