import { useState, useEffect, useCallback } from 'react';
import { energyApi } from '../api/client';
import { useWebSocket } from './useWebSocket';

export function useEnergy(username) {
  const [stats,    setStats]    = useState(null);
  const [readings, setReadings] = useState([]);
  const [alerts,   setAlerts]   = useState([]);
  const [loading,  setLoading]  = useState(true);
  const [wsLive,   setWsLive]   = useState(false);

  const load = useCallback(async () => {
    try {
      const [s, r, a] = await Promise.all([
        energyApi.getStats(),
        energyApi.getReadings(),
        energyApi.getAlerts(),
      ]);
      setStats(s.data);
      setReadings(r.data);
      setAlerts(a.data);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  useWebSocket(username, {
    onReading: r => {
      setWsLive(true);
      setReadings(prev => [r, ...prev].slice(0, 100));
      setStats(prev => prev ? {
        ...prev,
        totalToday:   +((prev.totalToday   || 0) + r.consumptionKwh).toFixed(2),
        totalReadings: (prev.totalReadings || 0) + 1,
        peakConsumption: Math.max(prev.peakConsumption || 0, r.consumptionKwh),
      } : prev);
    },
    onAlert: a => {
      setAlerts(prev => [a, ...prev]);
      setStats(prev => prev ? { ...prev, activeAlerts: (prev.activeAlerts || 0) + 1 } : prev);
    },
    onStats: s => setStats(s),
  });

  const acknowledge = useCallback(async (id) => {
    await energyApi.acknowledge(id);
    setAlerts(prev => prev.map(a => a.id === id ? { ...a, acknowledged: true } : a));
    setStats(prev => prev ? { ...prev, activeAlerts: Math.max(0, (prev.activeAlerts || 1) - 1) } : prev);
  }, []);

  const submitReading = useCallback(async (kwh, location) => {
    await energyApi.submit({ consumptionKwh: kwh, location, source: 'MANUAL' });
  }, []);

  const deleteReading = useCallback(async (id) => {
    await energyApi.deleteReading(id);
    setReadings(prev => prev.filter(r => r.id !== id));
  }, []);

  return {
    stats, readings, alerts, loading, wsLive,
    acknowledge, submitReading, deleteReading,
    reload: load,
  };
}
