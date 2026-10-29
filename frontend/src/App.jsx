import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { NotificationProvider, useNotifications } from './context/NotificationContext';

// Components
import { DemoBar } from './components/common/DemoBar';
import { Navbar } from './components/common/Navbar';
import { Footer } from './components/common/Footer';
import { NotificationDrawer } from './components/common/NotificationDrawer';

// Pages
import { LandingPage } from './pages/LandingPage';
import { ExplorePage } from './pages/ExplorePage';
import { ReportIssuePage } from './pages/ReportIssuePage';
import { IssueDetailPage } from './pages/IssueDetailPage';
import { TrackIssuePage } from './pages/TrackIssuePage';
import { CitizenDashboard } from './pages/CitizenDashboard';
import { AuthorityDashboard } from './pages/AuthorityDashboard';
import { WorkerDashboard } from './pages/WorkerDashboard';
import { AdminDashboard } from './pages/AdminDashboard';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';

import { Bell, X } from 'lucide-react';

// Toast Notification Banner for incoming STOMP alerts
const ToastBanner = () => {
  const { toastMessage, dismissToast } = useNotifications();
  if (!toastMessage) return null;

  return (
    <div className="fixed bottom-5 right-5 z-50 max-w-sm bg-navy-950 text-white p-4 rounded-2xl shadow-2xl border border-navy-700 flex items-start gap-3 animate-soft-pulse">
      <div className="p-2 rounded-xl bg-brand-600 text-white shrink-0 mt-0.5">
        <Bell className="w-4 h-4" />
      </div>
      <div className="flex-1 min-w-0">
        <h4 className="text-xs font-bold text-white truncate">{toastMessage.title}</h4>
        <p className="text-xs text-slate-300 line-clamp-2 mt-0.5">{toastMessage.message}</p>
      </div>
      <button
        onClick={dismissToast}
        className="text-slate-400 hover:text-white p-1 rounded-lg"
      >
        <X className="w-4 h-4" />
      </button>
    </div>
  );
};

export const App = () => {
  return (
    <AuthProvider>
      <NotificationProvider>
        <Router>
          <div className="min-h-screen flex flex-col bg-slate-50 font-sans antialiased text-slate-900 selection:bg-brand-500 selection:text-white">
            {/* Top 1-Click Role Switcher */}
            <DemoBar />

            {/* Sticky Main Navigation */}
            <Navbar />

            {/* Slide-over Notification Drawer */}
            <NotificationDrawer />

            {/* Floating Live WebSocket Alert Toast */}
            <ToastBanner />

            {/* Main Application Routes */}
            <main className="flex-1">
              <Routes>
                <Route path="/" element={<LandingPage />} />
                <Route path="/explore" element={<ExplorePage />} />
                <Route path="/report" element={<ReportIssuePage />} />
                <Route path="/issues/:id" element={<IssueDetailPage />} />
                <Route path="/track" element={<TrackIssuePage />} />

                {/* Role Specific Dashboards */}
                <Route path="/citizen/dashboard" element={<CitizenDashboard />} />
                <Route path="/authority/dashboard" element={<AuthorityDashboard />} />
                <Route path="/worker/dashboard" element={<WorkerDashboard />} />
                <Route path="/admin/dashboard" element={<AdminDashboard />} />

                {/* Authentication */}
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />

                {/* Fallback */}
                <Route path="*" element={<Navigate to="/" replace />} />
              </Routes>
            </main>

            {/* Footer */}
            <Footer />
          </div>
        </Router>
      </NotificationProvider>
    </AuthProvider>
  );
};

export default App;
