import React, { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../services/api';

const AuthContext = createContext(null);

export const DEMO_ACCOUNTS = [
  {
    role: 'CITIZEN',
    label: 'Citizen (Pulkit)',
    email: 'citizen@civicfix.org',
    name: 'Pulkit Sharma',
    badge: 'Student / Reporter',
    avatarBg: 'bg-emerald-600',
  },
  {
    role: 'AUTHORITY',
    label: 'Authority (Er. Verma)',
    email: 'authority@civicfix.org',
    name: 'Er. Shalini Verma',
    badge: 'Estate Dept Head',
    avatarBg: 'bg-indigo-600',
  },
  {
    role: 'WORKER',
    label: 'Worker (Ramesh)',
    email: 'worker@civicfix.org',
    name: 'Ramesh Kumar',
    badge: 'Estate Field Tech',
    avatarBg: 'bg-amber-600',
  },
  {
    role: 'ADMIN',
    label: 'Dean / Admin (Dr. Mehra)',
    email: 'admin@civicfix.org',
    name: 'Dr. Arvind Mehra',
    badge: 'Platform Administrator',
    avatarBg: 'bg-rose-600',
  },
];

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('civicfix_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [token, setToken] = useState(() => localStorage.getItem('civicfix_token'));
  const [loading, setLoading] = useState(true);

  // Initialize and verify user on mount
  useEffect(() => {
    const initAuth = async () => {
      const storedToken = localStorage.getItem('civicfix_token');
      if (storedToken) {
        try {
          const profile = await authApi.getCurrentUser();
          setUser(profile);
          localStorage.setItem('civicfix_user', JSON.stringify(profile));
        } catch (err) {
          console.warn('Failed to refresh user profile with token:', err.message);
          // If token failed against backend, keep local user if available or clear
          if (!localStorage.getItem('civicfix_user')) {
            logout();
          }
        }
      }
      setLoading(false);
    };

    initAuth();
  }, []);

  const login = async (email, password) => {
    setLoading(true);
    try {
      const res = await authApi.login(email, password);
      // res is AuthResponse { token, type, user }
      const authToken = res.token || res.accessToken;
      const authUser = res.user;

      localStorage.setItem('civicfix_token', authToken);
      localStorage.setItem('civicfix_user', JSON.stringify(authUser));
      setToken(authToken);
      setUser(authUser);
      return authUser;
    } catch (err) {
      console.error('Login error:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const register = async (userData) => {
    setLoading(true);
    try {
      const res = await authApi.register(userData);
      const authToken = res.token || res.accessToken;
      const authUser = res.user;

      localStorage.setItem('civicfix_token', authToken);
      localStorage.setItem('civicfix_user', JSON.stringify(authUser));
      setToken(authToken);
      setUser(authUser);
      return authUser;
    } catch (err) {
      console.error('Registration error:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    try {
      authApi.logout().catch(() => {});
    } catch (e) {}
    localStorage.removeItem('civicfix_token');
    localStorage.removeItem('civicfix_user');
    setToken(null);
    setUser(null);
  };

  const switchDemoUser = async (roleName) => {
    const demo = DEMO_ACCOUNTS.find((d) => d.role === roleName);
    if (!demo) return;

    try {
      return await login(demo.email, 'password123');
    } catch (err) {
      console.warn('Backend login for demo user failed (backend may be initializing), using local simulation fallback');
      const fallbackUser = {
        id: demo.role === 'ADMIN' ? 1 : demo.role === 'AUTHORITY' ? 2 : demo.role === 'WORKER' ? 3 : 5,
        email: demo.email,
        fullName: demo.name,
        role: demo.role,
        departmentId: demo.role === 'AUTHORITY' || demo.role === 'WORKER' ? 1 : null,
        departmentName: demo.role === 'AUTHORITY' || demo.role === 'WORKER' ? 'Estate & Infrastructure Maintenance' : null,
        isActive: true,
      };
      const mockToken = 'mock-jwt-token-' + demo.role.toLowerCase();
      localStorage.setItem('civicfix_token', mockToken);
      localStorage.setItem('civicfix_user', JSON.stringify(fallbackUser));
      setToken(mockToken);
      setUser(fallbackUser);
      return fallbackUser;
    }
  };

  const hasRole = (role) => {
    if (!user) return false;
    if (Array.isArray(role)) {
      return role.includes(user.role);
    }
    return user.role === role;
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        isAuthenticated: !!user,
        login,
        register,
        logout,
        switchDemoUser,
        hasRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
