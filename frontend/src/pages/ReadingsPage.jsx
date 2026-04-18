import { useState } from 'react';
import { useEnergy } from '../hooks/useEnergy';
import { Panel, PanelTitle, EmptyState, SEVERITY_COLOR } from '../components/UI';
import { List, Trash2, Filter } from 'lucide-react';

const SOURCE_COLOR = { MANUAL: '#6366f1', SIMULATED: '#f59e0b', SENSOR: '#22c55e' };

export default function ReadingsPage({ username }) {
  const { readings, loading, deleteReading } = useEnergy(username);
  const [filter, setFilter]   = useState('ALL');
  const [deleting, setDeleting] = useState(null);

  const sources = ['ALL', 'MANUAL', 'SIMULATED', 'SENSOR'];

  const filtered = filter === 'ALL'
    ? readings
    : readings.filter(r => r.source === filter);

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this reading?')) return;
    setDeleting(id);
    try { await deleteReading(id); }
    finally { setDeleting(null); }
  };

  return (
    <div style={s.page}>
      <Panel>
        <div style={s.header}>
          <PanelTitle><List size={14} /> All readings ({filtered.length})</PanelTitle>
          <div style={s.filters}>
            <Filter size={12} color="#475569" />
            {sources.map(src => (
              <button key={src}
                style={{ ...s.filterBtn, ...(filter === src ? s.filterActive : {}) }}
                onClick={() => setFilter(src)}>
                {src}
              </button>
            ))}
          </div>
        </div>

        {loading ? (
          <div style={s.loading}>Loading readings…</div>
        ) : filtered.length === 0 ? (
          <EmptyState icon="📊" message="No readings found" />
        ) : (
          <div style={s.tableWrap}>
            <table style={s.table}>
              <thead>
                <tr>
                  {['ID', 'Timestamp', 'Consumption', 'Location', 'Source', ''].map(h => (
                    <th key={h} style={s.th}>{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {filtered.map(r => (
                  <tr key={r.id} style={s.tr}>
                    <td style={s.td}>
                      <span style={s.idBadge}>#{r.id}</span>
                    </td>
                    <td style={{ ...s.td, fontFamily: 'monospace', fontSize: 11 }}>
                      {new Date(r.timestamp).toLocaleString('sv-SE')}
                    </td>
                    <td style={s.td}>
                      <span style={{ ...s.kwhBadge, color: r.consumptionKwh > 10 ? '#f87171' : '#818cf8' }}>
                        {r.consumptionKwh} kWh
                      </span>
                    </td>
                    <td style={{ ...s.td, color: '#94a3b8' }}>{r.location}</td>
                    <td style={s.td}>
                      <span style={{ ...s.sourceBadge, background: SOURCE_COLOR[r.source] + '20', color: SOURCE_COLOR[r.source] }}>
                        {r.source}
                      </span>
                    </td>
                    <td style={s.td}>
                      <button style={s.deleteBtn} disabled={deleting === r.id}
                        onClick={() => handleDelete(r.id)}>
                        <Trash2 size={13} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Panel>
    </div>
  );
}

const s = {
  page: {},
  header: { display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 14 },
  filters: { display: 'flex', alignItems: 'center', gap: 6 },
  filterBtn: {
    padding: '4px 10px', borderRadius: 6, border: '1px solid #334155',
    background: 'none', color: '#64748b', fontSize: 11, cursor: 'pointer', fontFamily: 'monospace',
  },
  filterActive: { background: '#1e3a5f', borderColor: '#3b82f6', color: '#93c5fd' },
  loading: { padding: '2rem', textAlign: 'center', color: '#475569', fontSize: 13 },
  tableWrap: { overflowX: 'auto' },
  table: { width: '100%', borderCollapse: 'collapse', fontSize: 13 },
  th: { padding: '8px 12px', textAlign: 'left', color: '#475569', fontSize: 11, fontFamily: 'monospace', textTransform: 'uppercase', letterSpacing: '0.05em', borderBottom: '1px solid #1e293b' },
  tr: { borderBottom: '1px solid #0f172a' },
  td: { padding: '10px 12px', color: '#e2e8f0', verticalAlign: 'middle' },
  idBadge: { fontSize: 11, color: '#475569', fontFamily: 'monospace' },
  kwhBadge: { fontWeight: 600, fontFamily: 'monospace' },
  sourceBadge: { fontSize: 11, fontFamily: 'monospace', padding: '2px 8px', borderRadius: 4 },
  deleteBtn: { background: 'none', border: 'none', color: '#475569', cursor: 'pointer', padding: 4, display: 'flex' },
};
