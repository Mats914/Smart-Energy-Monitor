import { useState } from 'react';
import { useEnergy } from '../hooks/useEnergy';
import { StatCard, Panel, PanelTitle, EmptyState } from '../components/UI';
import {
  AreaChart, Area, XAxis, YAxis, Tooltip,
  ResponsiveContainer, ReferenceLine,
} from 'recharts';
import { Plus, Activity } from 'lucide-react';

export default function Dashboard({ username }) {
  const { stats, readings, loading, wsLive, submitReading } = useEnergy(username);
  const [form, setForm]             = useState({ kwh: '', location: '' });
  const [submitting, setSubmitting] = useState(false);
  const [notice, setNotice]         = useState('');

  const handleSubmit = async e => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await submitReading(parseFloat(form.kwh), form.location || 'Main Meter');
      setForm({ kwh: '', location: '' });
      setNotice('ok');
      setTimeout(() => setNotice(''), 3000);
    } catch {
      setNotice('err');
    } finally {
      setSubmitting(false);
    }
  };

  const chartData = [...readings].reverse().slice(-24).map((r, i) => ({
    i: i + 1,
    value: r.consumptionKwh,
    time: new Date(r.timestamp).toLocaleTimeString('sv-SE', { hour: '2-digit', minute: '2-digit' }),
  }));

  return (
    <div style={s.page}>
      <div style={s.statsGrid}>
        <StatCard label="Today"         value={stats?.totalToday}      unit="kWh" color="#818cf8" />
        <StatCard label="This month"    value={stats?.totalThisMonth}  unit="kWh" color="#f59e0b" />
        <StatCard label="Peak"          value={stats?.peakConsumption} unit="kWh" color="#f87171" />
        <StatCard label="Avg / reading" value={stats?.averageDaily}    unit="kWh" color="#34d399" />
      </div>

      <div style={s.grid}>
        <Panel>
          <PanelTitle>
            <Activity size={14} />
            Consumption — last 24 readings
            {wsLive && <span style={s.live}>● LIVE</span>}
          </PanelTitle>
          {loading ? <div style={s.skeleton} /> : chartData.length === 0 ? (
            <EmptyState icon="⚡" message="No readings yet" />
          ) : (
            <ResponsiveContainer width="100%" height={220}>
              <AreaChart data={chartData} margin={{ top: 5, right: 10, left: -20, bottom: 0 }}>
                <defs>
                  <linearGradient id="g" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%"  stopColor="#6366f1" stopOpacity={0.3} />
                    <stop offset="95%" stopColor="#6366f1" stopOpacity={0}   />
                  </linearGradient>
                </defs>
                <XAxis dataKey="time" tick={{ fill:'#475569', fontSize:10 }} />
                <YAxis tick={{ fill:'#475569', fontSize:10 }} unit=" kWh" />
                <Tooltip
                  contentStyle={{ background:'#0f172a', border:'1px solid #334155', borderRadius:8, fontSize:12 }}
                  labelStyle={{ color:'#94a3b8' }}
                  itemStyle={{ color:'#818cf8' }}
                  formatter={v => [`${v} kWh`, 'Consumption']}
                />
                <ReferenceLine y={10} stroke="#f87171" strokeDasharray="4 3"
                  label={{ value:'alert threshold', fill:'#f87171', fontSize:9, position:'insideTopRight' }} />
                <Area type="monotone" dataKey="value" stroke="#6366f1" strokeWidth={2} fill="url(#g)" dot={false} />
              </AreaChart>
            </ResponsiveContainer>
          )}
        </Panel>

        <Panel>
          <PanelTitle><Plus size={14} /> Manual reading</PanelTitle>
          <form onSubmit={handleSubmit} style={s.form}>
            <label style={s.lbl}>Consumption (kWh)</label>
            <input style={s.input} type="number" step="0.1" min="0.1" max="1000" placeholder="e.g. 7.5"
              value={form.kwh} onChange={e => setForm(f => ({...f, kwh: e.target.value}))} required />
            <label style={s.lbl}>Location</label>
            <input style={s.input} placeholder="Main Meter"
              value={form.location} onChange={e => setForm(f => ({...f, location: e.target.value}))} />
            {notice === 'ok'  && <p style={{color:'#34d399',fontSize:12,margin:0}}>✅ Submitted via Kafka</p>}
            {notice === 'err' && <p style={{color:'#f87171',fontSize:12,margin:0}}>❌ Submission failed</p>}
            <button style={s.btn} disabled={submitting}>{submitting ? 'Submitting…' : '⚡ Submit via Kafka'}</button>
          </form>
          <div style={s.infoBox}>
            <div style={s.infoTitle}>Event flow</div>
            {['Submit → Kafka Producer', 'Consumer → persists to DB', 'WebSocket → live update', 'Auto-alert if > 10 kWh']
              .map(t => <div key={t} style={s.step}><span style={s.dot} />{t}</div>)}
          </div>
        </Panel>
      </div>

      <div style={s.summaryRow}>
        {[
          { label: 'Total readings', value: stats?.totalReadings ?? '—' },
          { label: 'Active alerts',  value: stats?.activeAlerts  ?? '—' },
          { label: 'Data pipeline',  value: 'Kafka + WebSocket'          },
          { label: 'Simulation',     value: 'Every 30 s'                 },
        ].map(({ label, value }) => (
          <div key={label} style={s.sumCard}>
            <div style={s.sumLabel}>{label}</div>
            <div style={s.sumValue}>{value}</div>
          </div>
        ))}
      </div>
    </div>
  );
}

const s = {
  page: { display:'flex', flexDirection:'column', gap:16 },
  statsGrid: { display:'grid', gridTemplateColumns:'repeat(4,1fr)', gap:12 },
  grid: { display:'grid', gridTemplateColumns:'1fr 280px', gap:16 },
  live: { marginLeft:'auto', fontSize:10, color:'#22c55e', fontFamily:'monospace' },
  skeleton: { height:220, background:'#0f172a', borderRadius:8 },
  form: { display:'flex', flexDirection:'column', gap:8, marginBottom:16 },
  lbl: { fontSize:11, color:'#64748b', fontFamily:'monospace', textTransform:'uppercase', letterSpacing:'.05em' },
  input: { padding:'9px 12px', borderRadius:8, border:'1px solid #334155', background:'#0f172a', color:'#f1f5f9', fontSize:13, outline:'none' },
  btn: { padding:10, borderRadius:8, background:'#4f46e5', color:'#fff', border:'none', fontWeight:600, fontSize:13, cursor:'pointer', marginTop:4 },
  infoBox: { background:'#0f172a', borderRadius:8, padding:12 },
  infoTitle: { fontSize:10, color:'#475569', fontFamily:'monospace', textTransform:'uppercase', marginBottom:8 },
  step: { display:'flex', alignItems:'center', gap:8, fontSize:12, color:'#64748b', marginBottom:4 },
  dot: { width:5, height:5, borderRadius:'50%', background:'#4f46e5', flexShrink:0 },
  summaryRow: { display:'grid', gridTemplateColumns:'repeat(4,1fr)', gap:12 },
  sumCard: { background:'#1e293b', borderRadius:10, padding:'12px 16px' },
  sumLabel: { fontSize:11, color:'#475569', fontFamily:'monospace', textTransform:'uppercase', marginBottom:4 },
  sumValue: { fontSize:14, fontWeight:600, color:'#e2e8f0' },
};
