import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useNotifications } from '../../context/NotificationContext';
import {
  ShieldCheck,
  PlusCircle,
  MapPin,
  Search,
  Bell,
  LogOut,
  User,
  Menu,
  X,
  LayoutDashboard,
  ChevronDown,
} from 'lucide-react';

export const Navbar = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const { unreadCount, toggleDrawer } = useNotifications();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [userDropdownOpen, setUserDropdownOpen] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    setUserDropdownOpen(false);
    navigate('/');
  };

  const getDashboardPath = () => {
    if (!user) return '/explore';
    switch (user.role) {
      case 'ADMIN':
        return '/admin/dashboard';
      case 'AUTHORITY':
        return '/authority/dashboard';
      case 'WORKER':
        return '/worker/dashboard';
      default:
        return '/citizen/dashboard';
    }
  };

  const isActive = (path) => location.pathname === path;

  return (
    <nav className="bg-white/95 backdrop-blur-md border-b border-slate-200/80 sticky top-[33px] z-30 transition-all">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo & Main Nav */}
          <div className="flex items-center gap-8">
            <Link to="/" className="flex items-center gap-2.5 group">
              <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-brand-600 to-emerald-400 flex items-center justify-center text-white shadow-md shadow-brand-500/20 group-hover:scale-105 transition-transform">
                <ShieldCheck className="w-5 h-5" />
              </div>
              <div className="flex flex-col">
                <span className="font-extrabold text-lg text-slate-900 tracking-tight leading-none">
                  Civic<span className="text-brand-600">Fix</span>
                </span>
                <span className="text-[10px] text-slate-500 font-medium tracking-wider uppercase">
                  Community Action
                </span>
              </div>
            </Link>

            {/* Desktop Navigation Links */}
            <div className="hidden md:flex items-center gap-1">
              <Link
                to="/explore"
                className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                  isActive('/explore')
                    ? 'text-brand-600 bg-brand-50/70 font-semibold'
                    : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'
                }`}
              >
                <span className="flex items-center gap-1.5">
                  <MapPin className="w-4 h-4 text-slate-400" />
                  Explore & Map
                </span>
              </Link>

              <Link
                to="/track"
                className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                  isActive('/track')
                    ? 'text-brand-600 bg-brand-50/70 font-semibold'
                    : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'
                }`}
              >
                <span className="flex items-center gap-1.5">
                  <Search className="w-4 h-4 text-slate-400" />
                  Track Tracking #
                </span>
              </Link>

              {isAuthenticated && (
                <Link
                  to={getDashboardPath()}
                  className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                    location.pathname.includes('/dashboard')
                      ? 'text-brand-600 bg-brand-50/70 font-semibold'
                      : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'
                  }`}
                >
                  <span className="flex items-center gap-1.5">
                    <LayoutDashboard className="w-4 h-4 text-slate-400" />
                    {user?.role === 'ADMIN'
                      ? 'Admin Console'
                      : user?.role === 'AUTHORITY'
                      ? 'Authority Portal'
                      : user?.role === 'WORKER'
                      ? 'Worker Tasks'
                      : 'My Dashboard'}
                  </span>
                </Link>
              )}
            </div>
          </div>

          {/* Right Action Buttons */}
          <div className="hidden md:flex items-center gap-3">
            {/* Report Button */}
            <Link
              to="/report"
              className="inline-flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-semibold bg-brand-600 text-white hover:bg-brand-700 shadow-sm shadow-brand-600/30 active:scale-95 transition-all"
            >
              <PlusCircle className="w-4 h-4" />
              Report Issue
            </Link>

            {isAuthenticated ? (
              <>
                {/* Notification Bell */}
                <button
                  onClick={toggleDrawer}
                  className="relative p-2 rounded-xl text-slate-600 hover:text-slate-900 hover:bg-slate-100 transition-colors"
                  aria-label="View notifications"
                >
                  <Bell className="w-5 h-5" />
                  {unreadCount > 0 && (
                    <span className="absolute top-1 right-1 flex items-center justify-center min-w-4 h-4 px-1 text-[10px] font-bold text-white bg-rose-500 rounded-full ring-2 ring-white">
                      {unreadCount > 9 ? '9+' : unreadCount}
                    </span>
                  )}
                </button>

                {/* User Dropdown */}
                <div className="relative">
                  <button
                    onClick={() => setUserDropdownOpen(!userDropdownOpen)}
                    className="flex items-center gap-2.5 p-1.5 pl-2 rounded-xl hover:bg-slate-100 transition-colors"
                  >
                    <div className="w-8 h-8 rounded-lg bg-slate-900 text-white flex items-center justify-center font-bold text-xs uppercase shadow-xs">
                      {user?.fullName ? user.fullName.charAt(0) : 'U'}
                    </div>
                    <div className="text-left hidden lg:block">
                      <div className="text-xs font-bold text-slate-900 truncate max-w-[120px]">
                        {user?.fullName || 'User'}
                      </div>
                      <div className="text-[10px] font-medium text-brand-600">
                        {user?.role}
                      </div>
                    </div>
                    <ChevronDown className="w-4 h-4 text-slate-400" />
                  </button>

                  {userDropdownOpen && (
                    <div className="absolute right-0 mt-2 w-56 rounded-2xl bg-white shadow-xl ring-1 ring-slate-200/80 p-1.5 z-50">
                      <div className="px-3 py-2 border-b border-slate-100 mb-1">
                        <p className="text-xs font-semibold text-slate-900">{user?.fullName}</p>
                        <p className="text-[11px] text-slate-500 truncate">{user?.email}</p>
                        <span className="inline-block mt-1 px-1.5 py-0.5 rounded text-[10px] font-semibold bg-brand-50 text-brand-700">
                          {user?.role}
                        </span>
                      </div>

                      <Link
                        to={getDashboardPath()}
                        onClick={() => setUserDropdownOpen(false)}
                        className="flex items-center gap-2 px-3 py-2 text-xs font-medium text-slate-700 hover:bg-slate-50 rounded-lg"
                      >
                        <LayoutDashboard className="w-4 h-4 text-slate-400" />
                        Dashboard
                      </Link>

                      <button
                        onClick={handleLogout}
                        className="w-full flex items-center gap-2 px-3 py-2 text-xs font-medium text-rose-600 hover:bg-rose-50 rounded-lg mt-1"
                      >
                        <LogOut className="w-4 h-4" />
                        Sign Out
                      </button>
                    </div>
                  )}
                </div>
              </>
            ) : (
              <div className="flex items-center gap-2">
                <Link
                  to="/login"
                  className="px-3.5 py-2 rounded-xl text-sm font-semibold text-slate-700 hover:text-slate-900 hover:bg-slate-100 transition-colors"
                >
                  Sign In
                </Link>
                <Link
                  to="/register"
                  className="px-3.5 py-2 rounded-xl text-sm font-semibold bg-navy-900 text-white hover:bg-navy-800 shadow-xs transition-colors"
                >
                  Sign Up
                </Link>
              </div>
            )}
          </div>

          {/* Mobile Menu Button */}
          <div className="flex md:hidden items-center gap-2">
            {isAuthenticated && (
              <button
                onClick={toggleDrawer}
                className="relative p-2 rounded-xl text-slate-600 hover:bg-slate-100"
              >
                <Bell className="w-5 h-5" />
                {unreadCount > 0 && (
                  <span className="absolute top-1 right-1 flex items-center justify-center w-4 h-4 text-[10px] font-bold text-white bg-rose-500 rounded-full">
                    {unreadCount}
                  </span>
                )}
              </button>
            )}

            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="p-2 rounded-xl text-slate-600 hover:bg-slate-100"
            >
              {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Menu Dropdown */}
      {mobileMenuOpen && (
        <div className="md:hidden border-t border-slate-200 bg-white p-4 space-y-2">
          <Link
            to="/explore"
            onClick={() => setMobileMenuOpen(false)}
            className="block px-3 py-2 rounded-lg text-sm font-medium text-slate-700 hover:bg-slate-50"
          >
            Explore Map & Issues
          </Link>
          <Link
            to="/track"
            onClick={() => setMobileMenuOpen(false)}
            className="block px-3 py-2 rounded-lg text-sm font-medium text-slate-700 hover:bg-slate-50"
          >
            Track by Tracking Number
          </Link>
          <Link
            to="/report"
            onClick={() => setMobileMenuOpen(false)}
            className="block px-3 py-2 rounded-lg text-sm font-semibold text-brand-600 bg-brand-50"
          >
            + Report an Issue
          </Link>

          {isAuthenticated ? (
            <>
              <Link
                to={getDashboardPath()}
                onClick={() => setMobileMenuOpen(false)}
                className="block px-3 py-2 rounded-lg text-sm font-medium text-slate-700 hover:bg-slate-50"
              >
                My Dashboard ({user?.role})
              </Link>
              <button
                onClick={() => {
                  handleLogout();
                  setMobileMenuOpen(false);
                }}
                className="w-full text-left px-3 py-2 rounded-lg text-sm font-medium text-rose-600 hover:bg-rose-50"
              >
                Sign Out
              </button>
            </>
          ) : (
            <div className="pt-2 border-t border-slate-100 flex gap-2">
              <Link
                to="/login"
                onClick={() => setMobileMenuOpen(false)}
                className="flex-1 text-center py-2 text-sm font-semibold text-slate-700 bg-slate-100 rounded-lg"
              >
                Sign In
              </Link>
              <Link
                to="/register"
                onClick={() => setMobileMenuOpen(false)}
                className="flex-1 text-center py-2 text-sm font-semibold text-white bg-brand-600 rounded-lg"
              >
                Sign Up
              </Link>
            </div>
          )}
        </div>
      )}
    </nav>
  );
};
