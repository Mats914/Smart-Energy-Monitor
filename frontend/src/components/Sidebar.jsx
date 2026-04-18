import { Zap, BarChart2, Bell, List, LogOut } from 'lucide-react';

const NAV = [
  { id: 'dashboard', icon: BarChart2, label: 'Dashboard' },
  { id: 'readings',  icon: List,      label: 'Readings'  },
  { id: 'alerts',    icon: Bell,      label: 'Alerts'    },
];

export default function Sidebar({ page, onNavigate, onLogout, username, alertCount }) {
  return (
    <div style={s.sidebar}>
      <div style={s.logo}>
        <div style={s.logoIcon}><Zap size={16} color="#f59e0b" /></div>
        <div>
          <div style={s.logoText}>SmartGrid</div>
          <div style={s.logoSub}>Monitor v2</div>
        </div>
      </div>

      <nav style={s.nav}>
        {NAV.map(({ id, icon: Icon, label }) => (
          <button
            key={id}
            style={{ ...s.navItem, ...(page === id ? s.navActive : {}) }}
            onClick={() => onNavigate(id)}
          >
            <Icon size={15} />
            <span>{label}</span>
            {id === 'alerts' && alertCount > 0 && (
              <span style={s.badge}>{alertCount}</span>
            )}
          </button>
        ))}
      </nav>

      <div style={s.bottom}>
        <div style={s.userRow}>
          <div style={s.avatar}>{username?.[0]?.toUpperCase()}</div>
          <div>
            <div style={s.userName}>{username}</div>
            <div style={s.userRole}>USER</div>
          </div>
        </div>
        <button style={s.logoutBtn} onClick={onLogout}>
          <LogOut size={14} />
          Sign out
        </button>
      </div>
    </div>
  );
}

const s = {
  sidebar: {
    width: 200, minHeight: '100vh', background: '#0f172a',
    borderRight: '1px solid #1e293b', padding: '20px 12px',
    display: 'flex', flexDirection: 'column', flexShrink: 0,
  },
  logo: { display: 'flex', alignItems: 'center', gap: 10, padding: '0 8px 24px' },
  logoIcon: { width: 32, height: 32, background: '#1e293b', borderRadius: 8, display: 'flex', alignItems: 'center', justifyContent: 'center' },
  logoText: { fontWeight: 700, fontSize: 14, color: '#f1f5f9', lineHeight: 1 },
  logoSub: { fontSize: 10, color: '#475569', fontFamily: 'monospace', marginTop: 2 },
  nav: { display: 'flex', flexDirection: 'column', gap: 2, flex: 1 },
  navItem: {
    display: 'flex', alignItems: 'center', gap: 10,
    padding: '9px 10px', borderRadius: 8, border: 'none',
    background: 'none', color: '#64748b', fontSize: 13,
    cursor: 'pointer', width: '100%', textAlign: 'left', position: 'relative',
  },
  navActive: { background: '#1e293b', color: '#e2e8f0' },
  badge: {
    marginLeft: 'auto', background: '#ef4444', color: '#fff',
    fontSize: 10, fontWeight: 700, borderRadius: 10,
    padding: '1px 6px', minWidth: 18, textAlign: 'center',
  },
  bottom: { borderTop: '1px solid #1e293b', paddingTop: 16, display: 'flex', flexDirection: 'column', gap: 10 },
  userRow: { display: 'flex', alignItems: 'center', gap: 10, padding: '0 4px' },
  avatar: {
    width: 30, height: 30, borderRadius: '50%',
    background: '#4f46e5', color: '#fff', fontSize: 13,
    fontWeight: 700, display: 'flex', alignItems: 'center', justifyContent: 'center',
  },
  userName: { fontSize: 12, color: '#e2e8f0', fontWeight: 600 },
  userRole: { fontSize: 10, color: '#475569', fontFamily: 'monospace' },
  logoutBtn: {
    display: 'flex', alignItems: 'center', gap: 8,
    padding: '8px 10px', borderRadius: 8, border: 'none',
    background: 'none', color: '#64748b', fontSize: 12,
    cursor: 'pointer', width: '100%',
  },
};
