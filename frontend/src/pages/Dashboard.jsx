import { useState, useEffect, useCallback } from 'react';
import { energyApi } from '../api/client';
import { useWebSocket } from '../hooks/useWebSocket';
import {
  AreaChart, Area, XAxis, YAxis, Tooltip,
  ResponsiveContainer, ReferenceLine
} from 'recharts';
import { Zap, Bell, TrendingUp, Activity, LogOut, Plus, CheckCircle } from 'lucide-react';

export default function Dashboard({ user, onLogout }) {
  const [stats, setStats]     = useState(null);
  const [readings, setReadings] = useState([]);
  const [alerts, setAlerts]   = useState([]);
  const [wsStatus, setWsStatus] = useState('connecting');
  const [form, setForm]       = useState({ consumptionKwh: '', location: '' });
  const [submitting, setSubmitting] = useState(false);
  const [tab, setTab]         = useState('chart');

  const load = useCallback(async () => {
    const [s, r, a] = await Promise.all([
      energyApi.getStats(),
      energyApi.getReadings(),
      energyApi.getAlerts(),
    ]);
    setStats(s.data);
    setReadings(r.data.slice(0, 24));
    setAlerts(a.data.slice(0, 10));
  }, []);

  useEffect(() => { load(); }, [load]);

  useWebSocket(user.username, {
    onReading: r => {
      setWsStatus('live');
      setReadings(prev => [r, ...prev].slice(0, 24));
    },
    onAlert: a => setAlerts(prev => [a, ...prev].slice(0, 10)),
    onStats: s => setStats(s),
  });

  const submit = async e => {
    e.preventDefault();
    if (!form.consumptionKwh) return;
    setSubmitting(true);
    try {
      await energyApi.submit({
        consumptionKwh: parseFloat(form.consumptionKwh),
        location: form.location || 'Main Meter',
        source: 'MANUAL',
      });
      setForm({ consumptionKwh: '', location: '' });
    } finally {
      setSubmitting(false);
    }
  };

  const acknowledge = async id => {
    await energyApi.acknowledge(id);
    setAlerts(prev => prev.map(a => a.id === id ? { ...a, acknowledged: true } : a));
  };

  const chartData = [...readings].reverse().map((r, i) => ({
    name: i + 1,
    value: r.consumptionKwh,
    time: new Date(r.timestamp).toLocaleTimeString('sv-SE', { hour: '2-digit', minute: '2-digit' }),
  }));

  const activeAlerts = alerts.filter(a => !a.acknowledged).length;

  return (
    <div style={s.page}>
      {/* Top bar */}
      <div style={s.topbar}>
        <div style={s.logo}>
          <div style={s.logoIcon}><Zap size={16} color="#f59e0b" /></div>
          <span style={s.logoText}>SmartGrid Monitor</span>
        </div>
        <div style={s.topRight}>
          <div style={{ ...s.pill, background: wsStatus === 'live' ? '#052e16' : '#1c1917', borderColor: wsStatus === 'live' ? '#166534' : '#44403c' }}>
            <div style={{ ...s.dot, background: wsStatus === 'live' ? '#22c55e' : '#78716c' }} />
            {wsStatus === 'live' ? 'LIVE' : 'CONNECTING'}
          </div>
          <div style={s.userBadge}>{user.username}</div>
          <button style={s.iconBtn} onClick={onLogout}><LogOut size={16} /></button>
        </div>
      </div>

      {/* Stats */}
      <div style={s.statsGrid}>
        {[
          { label: 'Today', value: stats?.totalToday ?? '—', unit: 'kWh', color: '#6366f1' },
          { label: 'This month', value: stats?.totalThisMonth ?? '—', unit: 'kWh', color: '#f59e0b' },
          { label: 'Peak reading', value: stats?.peakConsumption ?? '—', unit: 'kWh', color: '#ef4444' },
          { label: 'Active alerts', value: activeAlerts, unit: 'unread', color: activeAlerts > 0 ? '#f97316' : '#22c55e' },
        ].map(({ label, value, unit, color }) => (
          <div key={label} style={s.statCard}>
            <div style={s.statLabel}>{label}</div>
            <div style={{ ...s.statValue, color }}>{value}</div>
            <div style={s.statUnit}>{unit}</div>
          </div>
        ))}
      </div>

      {/* Main content */}
      <div style={s.mainGrid}>
        <div style={s.panel}>
          <div style={s.panelHeader}>
            <div style={s.tabs}>
              {['chart', 'readings'].map(t => (
                <button key={t} style={{ ...s.tab, ...(tab === t ? s.tabActive : {}) }} onClick={() => setTab(t)}>
                  {t === 'chart' ? 'Consumption chart' : 'All readings'}
                </button>
              ))}
            </div>
          </div>

          {tab === 'chart' ? (
            <ResponsiveContainer width="100%" height={220}>
              <AreaChart data={chartData} margin={{ top: 5, right: 10, left: -20, bottom: 0 }}>
                <defs>
                  <linearGradient id="grad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#6366f1" stopOpacity={0.3} />
                    <stop offset="95%" stopColor="#6366f1" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <XAxis dataKey="time" tick={{ fill: '#64748b', fontSize: 11 }} />
                <YAxis tick={{ fill: '#64748b', fontSize: 11 }} />
                <Tooltip
                  contentStyle={{ background: '#1e293b', border: '1px solid #334155', borderRadius: 8 }}
                  labelStyle={{ color: '#94a3b8', fontSize: 12 }}
                  itemStyle={{ color: '#818cf8' }}
                />
                <ReferenceLine y={10} stroke="#ef4444" strokeDasharray="4 3" label={{ value: 'threshold', fill: '#ef4444', fontSize: 10 }} />
                <Area type="monotone" dataKey="value" stroke="#6366f1" strokeWidth={2} fill="url(#grad)" />
              </AreaChart>
            </ResponsiveContainer>
          ) : (
            <div style={s.readingsList}>
              {readings.map(r => (
                <div key={r.id} style={s.readingRow}>
                  <div style={{ ...s.sourceDot, background: r.source === 'MANUAL' ? '#6366f1' : r.source === 'SIMULATED' ? '#f59e0b' : '#22c55e' }} />
                  <span style={s.readingTime}>{new Date(r.timestamp).toLocaleTimeString('sv-SE', { hour: '2-digit', minute: '2-digit' })}</span>
                  <span style={s.readingLocation}>{r.location}</span>
                  <div style={s.readingBarWrap}>
                    <div style={{ ...s.readingBar, width: `${Math.min((r.consumptionKwh / 30) * 100, 100)}%`, background: r.consumptionKwh > 10 ? '#ef4444' : '#6366f1' }} />
                  </div>
                  <span style={{ ...s.readingVal, color: r.consumptionKwh > 10 ? '#f87171' : '#818cf8' }}>{r.consumptionKwh} kWh</span>
                </div>
              ))}
            </div>
          )}
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          {/* Submit form */}
          <div style={s.panel}>
            <div style={s.panelTitle}><Plus size={14} /> Add reading</div>
            <form onSubmit={submit} style={s.form}>
              <input
                style={s.input}
                type="number"
                step="0.1"
                min="0.1"
                max="1000"
                placeholder="Consumption (kWh)"
                value={form.consumptionKwh}
                onChange={e => setForm(f => ({ ...f, consumptionKwh: e.target.value }))}
                required
              />
              <input
                style={s.input}
                placeholder="Location (optional)"
                value={form.location}
                onChange={e => setForm(f => ({ ...f, location: e.target.value }))}
              />
              <button style={s.submitBtn} disabled={submitting}>
                {submitting ? 'Submitting...' : 'Submit via Kafka'}
              </button>
            </form>
          </div>

          {/* Alerts */}
          <div style={{ ...s.panel, flex: 1 }}>
            <div style={s.panelTitle}><Bell size={14} /> Recent alerts</div>
            <div style={s.alertsList}>
              {alerts.slice(0, 5).map(a => (
                <div key={a.id} style={{ ...s.alertRow, opacity: a.acknowledged ? 0.5 : 1 }}>
                  <div style={{ ...s.severityDot, background: severityColor(a.severity) }} />
                  <div style={{ flex: 1 }}>
                    <div style={s.alertSeverity}>{a.severity}</div>
                    <div style={s.alertVal}>{a.triggerValue?.toFixed(1)} kWh</div>
                  </div>
                  {!a.acknowledged && (
                    <button style={s.ackBtn} onClick={() => acknowledge(a.id)}>
                      <CheckCircle size={14} />
                    </button>
                  )}
                </div>
              ))}
              {alerts.length === 0 && <p style={s.empty}>No alerts yet</p>}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

const severityColor = sev => ({ CRITICAL: '#ef4444', HIGH: '#f97316', MEDIUM: '#eab308', LOW: '#22c55e' }[sev] ?? '#64748b');

const s = {
  page: { minHeight: '100vh', background: '#0f172a', color: '#f1f5f9', fontFamily: 'system-ui, sans-serif', padding: 20 },
  topbar: { display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 24, paddingBottom: 16, borderBottom: '1px solid #1e293b' },
  logo: { display: 'flex', alignItems: 'center', gap: 10 },
  logoIcon: { width: 32, height: 32, background: '#1e293b', borderRadius: 8, display: 'flex', alignItems: 'center', justifyContent: 'center' },
  logoText: { fontWeight: 700, fontSize: 15 },
  topRight: { display: 'flex', alignItems: 'center', gap: 10 },
  pill: { display: 'flex', alignItems: 'center', gap: 6, padding: '4px 12px', borderRadius: 20, border: '1px solid', fontSize: 11, fontFamily: 'monospace', color: '#94a3b8' },
  dot: { width: 7, height: 7, borderRadius: '50%', animation: 'none' },
  userBadge: { fontSize: 13, color: '#94a3b8', background: '#1e293b', padding: '4px 12px', borderRadius: 6 },
  iconBtn: { background: 'none', border: 'none', color: '#64748b', cursor: 'pointer', display: 'flex', padding: 6 },
  statsGrid: { display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 12, marginBottom: 20 },
  statCard: { background: '#1e293b', borderRadius: 12, padding: 16 },
  statLabel: { fontSize: 11, color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 8, fontFamily: 'monospace' },
  statValue: { fontSize: 26, fontWeight: 700, letterSpacing: '-1px', lineHeight: 1 },
  statUnit: { fontSize: 12, color: '#475569', marginTop: 4, fontFamily: 'monospace' },
  mainGrid: { display: 'grid', gridTemplateColumns: '1fr 320px', gap: 16 },
  panel: { background: '#1e293b', borderRadius: 12, padding: 16 },
  panelHeader: { marginBottom: 16 },
  panelTitle: { fontSize: 13, fontWeight: 600, color: '#94a3b8', marginBottom: 14, display: 'flex', alignItems: 'center', gap: 6 },
  tabs: { display: 'flex', gap: 4 },
  tab: { padding: '6px 14px', borderRadius: 6, border: 'none', background: 'transparent', color: '#64748b', fontSize: 13, cursor: 'pointer' },
  tabActive: { background: '#0f172a', color: '#e2e8f0' },
  readingsList: { display: 'flex', flexDirection: 'column', gap: 6, maxHeight: 220, overflowY: 'auto' },
  readingRow: { display: 'flex', alignItems: 'center', gap: 8, padding: '7px 10px', borderRadius: 8, background: '#0f172a' },
  sourceDot: { width: 6, height: 6, borderRadius: '50%', flexShrink: 0 },
  readingTime: { fontSize: 11, color: '#475569', fontFamily: 'monospace', minWidth: 38 },
  readingLocation: { fontSize: 11, color: '#64748b', minWidth: 70, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' },
  readingBarWrap: { flex: 1, height: 4, background: '#1e293b', borderRadius: 2, overflow: 'hidden' },
  readingBar: { height: '100%', borderRadius: 2, transition: 'width 0.4s' },
  readingVal: { fontSize: 12, fontFamily: 'monospace', fontWeight: 500, minWidth: 60, textAlign: 'right' },
  form: { display: 'flex', flexDirection: 'column', gap: 10 },
  input: { padding: '9px 12px', borderRadius: 8, border: '1px solid #334155', background: '#0f172a', color: '#f1f5f9', fontSize: 13, outline: 'none' },
  submitBtn: { padding: 10, borderRadius: 8, background: '#4f46e5', color: '#fff', border: 'none', fontWeight: 600, fontSize: 13, cursor: 'pointer' },
  alertsList: { display: 'flex', flexDirection: 'column', gap: 8 },
  alertRow: { display: 'flex', alignItems: 'center', gap: 10, padding: '8px 10px', borderRadius: 8, background: '#0f172a' },
  severityDot: { width: 8, height: 8, borderRadius: '50%', flexShrink: 0 },
  alertSeverity: { fontSize: 10, fontFamily: 'monospace', color: '#64748b', textTransform: 'uppercase' },
  alertVal: { fontSize: 14, fontWeight: 600, color: '#f1f5f9' },
  ackBtn: { background: 'none', border: 'none', color: '#22c55e', cursor: 'pointer', padding: 2 },
  empty: { fontSize: 13, color: '#475569', textAlign: 'center', padding: '1rem' },
};
