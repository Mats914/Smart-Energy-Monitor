export function StatusPill({ live }) {
  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: 6,
      padding: '4px 12px', borderRadius: 20,
      border: `1px solid ${live ? '#166534' : '#44403c'}`,
      background: live ? '#052e16' : '#1c1917',
      fontSize: 11, fontFamily: 'monospace', color: '#94a3b8',
    }}>
      <div style={{
        width: 7, height: 7, borderRadius: '50%',
        background: live ? '#22c55e' : '#78716c',
        animation: live ? 'pulse 2s infinite' : 'none',
      }} />
      {live ? 'LIVE' : 'CONNECTING'}
      <style>{`@keyframes pulse{0%,100%{opacity:1}50%{opacity:.3}}`}</style>
    </div>
  );
}

export function StatCard({ label, value, unit, color }) {
  return (
    <div style={{ background: '#1e293b', borderRadius: 12, padding: 16 }}>
      <div style={{ fontSize: 11, color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 8, fontFamily: 'monospace' }}>
        {label}
      </div>
      <div style={{ fontSize: 26, fontWeight: 700, letterSpacing: '-1px', lineHeight: 1, color }}>
        {value ?? '—'}
      </div>
      <div style={{ fontSize: 12, color: '#475569', marginTop: 4, fontFamily: 'monospace' }}>
        {unit}
      </div>
    </div>
  );
}

export function EmptyState({ icon, message }) {
  return (
    <div style={{ padding: '2rem', textAlign: 'center', color: '#475569', fontSize: 14 }}>
      <div style={{ fontSize: 28, marginBottom: 8 }}>{icon}</div>
      {message}
    </div>
  );
}

export function Panel({ children, style }) {
  return (
    <div style={{ background: '#1e293b', borderRadius: 12, padding: 16, ...style }}>
      {children}
    </div>
  );
}

export function PanelTitle({ children }) {
  return (
    <div style={{ fontSize: 13, fontWeight: 600, color: '#94a3b8', marginBottom: 14, display: 'flex', alignItems: 'center', gap: 6 }}>
      {children}
    </div>
  );
}

export const SEVERITY_COLOR = {
  CRITICAL: '#ef4444',
  HIGH:     '#f97316',
  MEDIUM:   '#eab308',
  LOW:      '#22c55e',
};
