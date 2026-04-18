import { useState } from 'react';
import { useAuth }    from './hooks/useAuth';
import { useEnergy }  from './hooks/useEnergy';
import Sidebar        from './components/Sidebar';
import LoginPage      from './pages/LoginPage';
import RegisterPage   from './pages/RegisterPage';
import Dashboard      from './pages/Dashboard';
import ReadingsPage   from './pages/ReadingsPage';
import AlertsPage     from './pages/AlertsPage';

function AppShell({ user, onLogout }) {
  const [page, setPage] = useState('dashboard');
  const { stats }       = useEnergy(user.username);
  const alertCount      = stats?.activeAlerts ?? 0;

  const renderPage = () => {
    if (page === 'readings') return <ReadingsPage username={user.username} />;
    if (page === 'alerts')   return <AlertsPage   username={user.username} />;
    return <Dashboard username={user.username} />;
  };

  return (
    <div style={{ display:'flex', minHeight:'100vh', background:'#0f172a', color:'#f1f5f9', fontFamily:'system-ui,sans-serif' }}>
      <Sidebar page={page} onNavigate={setPage} onLogout={onLogout} username={user.username} alertCount={alertCount} />
      <main style={{ flex:1, padding:24, overflowY:'auto' }}>
        <div style={{ marginBottom:20 }}>
          <h1 style={{ fontSize:20, fontWeight:700, margin:0, textTransform:'capitalize' }}>{page}</h1>
          <p style={{ fontSize:11, color:'#475569', fontFamily:'monospace', margin:'4px 0 0' }}>
            Java 17 · Spring Boot 3 · Kafka · WebSocket · PostgreSQL · Docker
          </p>
        </div>
        {renderPage()}
      </main>
    </div>
  );
}

export default function App() {
  const { user, login, register, logout, error, loading } = useAuth();
  const [authPage, setAuthPage] = useState('login');

  if (user) return <AppShell user={user} onLogout={logout} />;

  return authPage === 'login'
    ? <LoginPage    onLogin={login}       onSwitchToRegister={() => setAuthPage('register')} error={error} loading={loading} />
    : <RegisterPage onRegister={register} onSwitchToLogin={()    => setAuthPage('login')}    error={error} loading={loading} />;
}
