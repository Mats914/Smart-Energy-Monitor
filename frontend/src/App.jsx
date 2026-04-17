import { useState } from 'react';
import { useAuth } from './hooks/useAuth';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import Dashboard from './pages/Dashboard';

export default function App() {
  const { user, login, register, logout, error, loading } = useAuth();
  const [page, setPage] = useState('login');

  if (user) {
    return <Dashboard user={user} onLogout={logout} />;
  }

  return page === 'login'
    ? <LoginPage
        onLogin={login}
        onSwitchToRegister={() => setPage('register')}
        error={error}
        loading={loading}
      />
    : <RegisterPage
        onRegister={register}
        onSwitchToLogin={() => setPage('login')}
        error={error}
        loading={loading}
      />;
}
