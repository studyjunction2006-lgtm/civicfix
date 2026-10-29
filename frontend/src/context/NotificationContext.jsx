import React, { createContext, useContext, useState, useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useAuth } from './AuthContext';
import { notificationApi } from '../services/api';

const NotificationContext = createContext(null);

export const NotificationProvider = ({ children }) => {
  const { user, isAuthenticated } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const [toastMessage, setToastMessage] = useState(null);
  const stompClientRef = useRef(null);

  // Fetch initial notifications when user logs in
  const fetchNotifications = async () => {
    if (!isAuthenticated || !user) return;
    try {
      const res = await notificationApi.getMyNotifications({ page: 0, size: 25 });
      const items = res.content || res || [];
      setNotifications(items);

      const unreadRes = await notificationApi.getUnreadCount();
      setUnreadCount(unreadRes.unreadCount ?? items.filter((n) => !n.isRead).length);
    } catch (err) {
      console.warn('Failed to load notifications via REST:', err.message);
    }
  };

  useEffect(() => {
    if (!isAuthenticated || !user) {
      setNotifications([]);
      setUnreadCount(0);
      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
      }
      return;
    }

    fetchNotifications();

    // Setup STOMP WebSocket connection
    const wsUrl = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws';
    const client = new Client({
      webSocketFactory: () => new SockJS(wsUrl),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        // Subscribe to user-specific topic
        client.subscribe(`/topic/notifications/${user.id}`, (message) => {
          try {
            const newNotification = JSON.parse(message.body);
            setNotifications((prev) => [newNotification, ...prev]);
            setUnreadCount((prev) => prev + 1);

            // Trigger Toast
            setToastMessage({
              title: newNotification.title,
              message: newNotification.message,
              type: newNotification.notificationType,
            });

            // Auto-hide toast after 5s
            setTimeout(() => {
              setToastMessage(null);
            }, 5000);
          } catch (err) {
            console.error('Error parsing WS notification payload:', err);
          }
        });
      },
      onStompError: (frame) => {
        console.warn('STOMP broker reported error: ' + frame.headers['message']);
      },
      onWebSocketError: (event) => {
        // Soft fail without crashing app if backend ws is unavailable
      },
    });

    try {
      client.activate();
      stompClientRef.current = client;
    } catch (e) {
      console.warn('WebSocket activate error:', e);
    }

    return () => {
      if (stompClientRef.current) {
        try {
          stompClientRef.current.deactivate();
        } catch (e) {}
      }
    };
  }, [isAuthenticated, user?.id]);

  const markAsRead = async (id) => {
    try {
      await notificationApi.markAsRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, isRead: true } : n))
      );
      setUnreadCount((prev) => Math.max(0, prev - 1));
    } catch (err) {
      // Optimistic update
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, isRead: true } : n))
      );
      setUnreadCount((prev) => Math.max(0, prev - 1));
    }
  };

  const markAllAsRead = async () => {
    try {
      await notificationApi.markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
      setUnreadCount(0);
    } catch (err) {
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
      setUnreadCount(0);
    }
  };

  const toggleDrawer = () => setIsDrawerOpen((prev) => !prev);
  const closeDrawer = () => setIsDrawerOpen(false);

  return (
    <NotificationContext.Provider
      value={{
        notifications,
        unreadCount,
        isDrawerOpen,
        toggleDrawer,
        closeDrawer,
        markAsRead,
        markAllAsRead,
        refreshNotifications: fetchNotifications,
        toastMessage,
        dismissToast: () => setToastMessage(null),
      }}
    >
      {children}
    </NotificationContext.Provider>
  );
};

export const useNotifications = () => {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error('useNotifications must be used within a NotificationProvider');
  }
  return context;
};
