import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth, DEMO_ACCOUNTS } from '../context/AuthContext';
import { ShieldCheck, Mail, Lock, ArrowRight, Sparkles } from 'lucide-react';

export const LoginPage = () => {
  const { login, switchDemoUser } = useAuth();
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await login(email, password);
      navigate('/explore');
    } catch (err) {
      setError(err.message || 'Invalid credentials');
    } finally {
      setLoading(false);
    }
  };

  const handleDemoLogin = async (role) => {
    setError(null);
    setLoading(true);
    try {
      await switchDemoUser(role);
      navigate('/explore');
    } catch (err) {
      setError(err.message || 'Demo login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[80vh] flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-md space-y-6">
        {/* Header */}
        <div className="text-center space-y-2">
          <div className="inline-flex w-12 h-12 rounded-2xl bg-gradient-to-tr from-brand-600 to-emerald-400 items-center justify-center text-white shadow-lg shadow-brand-500/25 mb-2">
            <ShieldCheck className="w-7 h-7" />
          </div>
          <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">
            Sign In to CivicFix
          </h1>
          <p className="text-xs text-slate-500">
            Access transparent campus issue reporting, dispatching, and resolution audits.
          </p>
        </div>

        {/* Demo Fast Login Selector */}
        <div className="bg-slate-50 p-4 rounded-2xl border border-slate-200/90 space-y-2.5">
          <div className="flex items-center gap-1.5 text-xs font-semibold text-slate-700">
            <Sparkles className="w-3.5 h-3.5 text-brand-600" />
            <span>Instant Demo Accounts (Click to Login):</span>
          </div>
          <div className="grid grid-cols-2 gap-2 text-xs">
            {DEMO_ACCOUNTS.map((acc) => (
              <button
                key={acc.role}
                type="button"
                onClick={() => handleDemoLogin(acc.role)}
                className="p-2.5 rounded-xl border border-slate-200 bg-white hover:border-brand-500 hover:shadow-xs transition-all text-left group"
              >
                <div className="font-bold text-slate-900 group-hover:text-brand-600 truncate">
                  {acc.name.split(' ')[0]} ({acc.role})
                </div>
                <div className="text-[10px] text-slate-400 truncate">{acc.badge}</div>
              </button>
            ))}
          </div>
        </div>

        {/* Form Card */}
        <div className="bg-white p-6 sm:p-8 rounded-2xl border border-slate-200/90 shadow-sm space-y-5">
          {error && (
            <div className="p-3 bg-rose-50 border border-rose-200 text-rose-700 text-xs rounded-xl">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4 text-xs">
            <div>
              <label className="block font-semibold text-slate-700 mb-1">Email Address</label>
              <div className="relative">
                <Mail className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="name@civicfix.org"
                  required
                  className="w-full pl-9 pr-3 py-2.5 rounded-xl border border-slate-300 focus:ring-2 focus:ring-brand-500 focus:border-brand-500 outline-none"
                />
              </div>
            </div>

            <div>
              <label className="block font-semibold text-slate-700 mb-1">Password</label>
              <div className="relative">
                <Lock className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  required
                  className="w-full pl-9 pr-3 py-2.5 rounded-xl border border-slate-300 focus:ring-2 focus:ring-brand-500 focus:border-brand-500 outline-none"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 rounded-xl font-bold text-white bg-brand-600 hover:bg-brand-700 shadow-md shadow-brand-600/20 active:scale-95 transition-all text-xs flex items-center justify-center gap-1.5 disabled:opacity-50"
            >
              <span>{loading ? 'Authenticating...' : 'Sign In'}</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </form>

          <div className="text-center pt-2 border-t border-slate-100 text-xs text-slate-500">
            Don't have an account?{' '}
            <Link to="/register" className="font-semibold text-brand-600 hover:underline">
              Create citizen account
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};
