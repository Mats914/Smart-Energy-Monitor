import { useState, useCallback } from 'react';
import { authApi } from '../api/client';

export function useAuth() {
  const [user, setUser] = useState(() => {
    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');
    return token ? { token, username } : null;
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const login = useCallback(async (username, password) => {
    setLoading(true); setError('');
    try {
      const res = await authApi.login({ username, password });
      localStorage.setItem('token', res.data.token);
      localStorage.setItem('username', res.data.username);
      setUser({ token: res.data.token, username: res.data.username });
      return true;
    } catch (e) {
      setError(e.response?.data?.message || 'Login failed');
      return false;
    } finally {
      setLoading(false);
    }
  }, []);

  const register = useCallback(async (username, email, password) => {
    setLoading(true); setError('');
    try {
      const res = await authApi.register({ username, email, password });
      localStorage.setItem('token', res.data.token);
      localStorage.setItem('username', res.data.username);
      setUser({ token: res.data.token, username: res.data.username });
      return true;
    } catch (e) {
      setError(e.response?.data?.message || 'Registration failed');
      return false;
    } finally {
      setLoading(false);
    }
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    setUser(null);
  }, []);

  return { user, login, register, logout, error, loading };
}
