import { useEffect, useRef, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export function useWebSocket(username, { onReading, onAlert, onStats }) {
  const clientRef = useRef(null);

  const connect = useCallback(() => {
    if (!username) return;

    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      reconnectDelay: 5000,
      onConnect: () => {
        console.log('✅ WebSocket connected');

        client.subscribe(`/topic/readings/${username}`, msg => {
          const update = JSON.parse(msg.body);
          if (update.type === 'READING') onReading?.(update.payload);
        });

        client.subscribe(`/topic/alerts/${username}`, msg => {
          const update = JSON.parse(msg.body);
          if (update.type === 'ALERT') onAlert?.(update.payload);
        });

        client.subscribe(`/topic/stats/${username}`, msg => {
          const update = JSON.parse(msg.body);
          if (update.type === 'STATS') onStats?.(update.payload);
        });
      },
      onDisconnect: () => console.log('❌ WebSocket disconnected'),
    });

    client.activate();
    clientRef.current = client;
  }, [username]);

  useEffect(() => {
    connect();
    return () => clientRef.current?.deactivate();
  }, [connect]);
}
