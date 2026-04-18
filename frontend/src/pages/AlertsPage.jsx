import { useState } from 'react';
import { useEnergy } from '../hooks/useEnergy';
import { Panel, PanelTitle, EmptyState, SEVERITY_COLOR } from '../components/UI';
import { Bell, CheckCircle, Filter } from 'lucide-react';

const SEVERITY_BG = {
  CRITICAL: '#450a0a',
  HIGH:     '#431407',
  MEDIUM:   '#422006',
  LOW:      '#052e16',
};

export default function AlertsPage({ username }) {
  const { alerts, loading, acknowledge } = useEnergy(username);
  const [filter, setFilter] = useState('ACTIVE');
  const [acking, setAcking] = useState(null);

  const displayed = alerts.filter(a =>
    filter === 'ALL'    ? true :
    filter === 'ACTIVE' ? !a.acknowledged :
    a.acknowledged
  );

  const handleAck = async (id) => {
    setAcking(id);
    try { await acknowledge(id); }
    finally { setAcking(null); }
  };

  const counts = {
    ACTIVE: alerts.filter(a => !a.acknowledged).length,
    ALL:    alerts.length,
  };

  return (
    <div style={s.page}>
      {/* Summary cards */}
      <div style={s.summaryRow}>
        {Object.entries(SEVERITY_COLOR).map(([sev, color]) => {
          const cnt = alerts.filter(a => a.severity === sev && !a.acknowledged).length;
          return (
            <div key={sev} style={{ ...s.sevCard, borderLeft: `3px solid ${color}` }}>
              <div style={{ fontSize: 11, color: '#64748b', fontFamily: 'monospace', marginBottom: 4 }}>{sev}</div>
              <div style={{ fontSize: 22, fontWeight: 700, color }}>{cnt}</div>
              <div style={{ fontSize: 11, color: '#475569' }}>active</div>
            </div>
          );
        })}
      </div>

      <Panel>
        <div style={s.header}>
          <PanelTitle><Bell size={14} /> Alerts ({displayed.length})</PanelTitle>
          <div style={s.filters}>
            <Filter size={12} color="#475569" />
            {['ACTIVE', 'RESOLVED', 'ALL'].map(f => (
              <button key={f}
                style={{ ...s.filterBtn, ...(filter === f ? s.filterActive : {}) }}
                onClick={() => setFilter(f)}>
                {f} {f !== 'RESOLVED' && counts[f] != null ? `(${counts[f]})` : ''}
              </button>
            ))}
          </div>
        </div>

        {loading ? (
          <div style={s.loading}>Loading alerts…</div>
        ) : displayed.length === 0 ? (
          <EmptyState icon="✅" message={filter === 'ACTIVE' ? 'No active alerts — all clear!' : 'No alerts found'} />
        ) : (
          <div style={s.list}>
            {displayed.map(a => (
              <div key={a.id} style={{
                ...s.alertCard,
                background: a.acknowledged ? '#0f172a' : SEVERITY_BG[a.severity] || '#0f172a',
                borderLeft: `3px solid ${a.acknowledged ? '#1e293b' : SEVERITY_COLOR[a.severity]}`,
                opacity: a.acknowledged ? 0.6 : 1,
              }}>
                <div style={s.alertTop}>
                  <span style={{ ...s.badge, color: SEVERITY_COLOR[a.severity], background: SEVERITY_COLOR[a.severity] + '20' }}>
                    {a.severity}
                  </span>
                  <span style={s.timestamp}>
                    {new Date(a.createdAt).toLocaleString('sv-SE')}
                  </span>
                  {a.acknowledged && <span style={s.resolvedBadge}>✓ RESOLVED</span>}
                </div>
                <div style={s.alertBody}>
                  <div style={s.alertMsg}>{a.message}</div>
                  <div style={s.alertMeta}>
                    Trigger: <strong style={{ color: SEVERITY_COLOR[a.severity] }}>{a.triggerValue?.toFixed(2)} kWh</strong>
                    &nbsp;·&nbsp; Threshold: {a.threshold} kWh
                  </div>
                </div>
                {!a.acknowledged && (
                  <button style={s.ackBtn} disabled={acking === a.id} onClick={() => handleAck(a.id)}>
                    <CheckCircle size={14} />
                    {acking === a.id ? 'Acknowledging…' : 'Acknowledge'}
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </Panel>
    </div>
  );
}

const s = {
  page: { display: 'flex', flexDirection: 'column', gap: 16 },
  summaryRow: { display: 'grid', gridTemplateColumns: 'repeat(4,1fr)', gap: 12 },
  sevCard: { background: '#1e293b', borderRadius: 10, padding: '12px 16px' },
  header: { display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 14 },
  filters: { display: 'flex', alignItems: 'center', gap: 6 },
  filterBtn: { padding: '4px 10px', borderRadius: 6, border: '1px solid #334155', background: 'none', color: '#64748b', fontSize: 11, cursor: 'pointer', fontFamily: 'monospace' },
  filterActive: { background: '#1e3a5f', borderColor: '#3b82f6', color: '#93c5fd' },
  loading: { padding: '2rem', textAlign: 'center', color: '#475569', fontSize: 13 },
  list: { display: 'flex', flexDirection: 'column', gap: 10 },
  alertCard: { borderRadius: 10, padding: '12px 14px', display: 'flex', flexDirection: 'column', gap: 8 },
  alertTop: { display: 'flex', alignItems: 'center', gap: 10 },
  badge: { fontSize: 10, fontFamily: 'monospace', fontWeight: 700, padding: '2px 8px', borderRadius: 4 },
  timestamp: { fontSize: 11, color: '#475569', fontFamily: 'monospace' },
  resolvedBadge: { marginLeft: 'auto', fontSize: 10, color: '#22c55e', fontFamily: 'monospace' },
  alertBody: { display: 'flex', flexDirection: 'column', gap: 4 },
  alertMsg: { fontSize: 13, color: '#e2e8f0' },
  alertMeta: { fontSize: 12, color: '#64748b' },
  ackBtn: {
    display: 'flex', alignItems: 'center', gap: 6,
    padding: '6px 12px', borderRadius: 6, border: '1px solid #166534',
    background: '#052e16', color: '#22c55e', fontSize: 12, cursor: 'pointer',
    alignSelf: 'flex-start',
  },
};
