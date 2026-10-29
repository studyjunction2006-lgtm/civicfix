import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { issueApi } from '../services/api';
import { Search, ArrowRight, ShieldCheck, CheckCircle2, Clock } from 'lucide-react';

export const TrackIssuePage = () => {
  const [trackingNumber, setTrackingNumber] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const handleTrack = async (e) => {
    e.preventDefault();
    const query = trackingNumber.trim();
    if (!query) return;

    setError(null);
    setLoading(true);
    try {
      const issue = await issueApi.getIssueByTrackingNumber(query);
      navigate(`/issues/${issue.id}`);
    } catch (err) {
      setError(`No issue found with tracking number "${query}". Please check the ID and try again.`);
    } finally {
      setLoading(false);
    }
  };

  const handleQuickLookup = (num) => {
    setTrackingNumber(num);
  };

  return (
    <div className="max-w-2xl mx-auto px-4 py-16 space-y-8">
      <div className="text-center space-y-3">
        <div className="inline-flex w-12 h-12 rounded-2xl bg-brand-50 text-brand-600 items-center justify-center mb-1">
          <Search className="w-6 h-6" />
        </div>
        <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">
          Track Your Maintenance Ticket
        </h1>
        <p className="text-sm text-slate-500 max-w-md mx-auto">
          Every report has a unique tracking identifier. Enter your tracking number below to inspect live status, worker dispatch, and repair audit logs.
        </p>
      </div>

      <div className="bg-white p-6 sm:p-8 rounded-2xl border border-slate-200/90 shadow-sm space-y-4">
        {error && (
          <div className="p-3 bg-rose-50 border border-rose-200 text-rose-700 text-xs rounded-xl">
            {error}
          </div>
        )}

        <form onSubmit={handleTrack} className="space-y-4">
          <div className="relative">
            <Search className="w-5 h-5 text-slate-400 absolute left-3.5 top-3.5" />
            <input
              type="text"
              value={trackingNumber}
              onChange={(e) => setTrackingNumber(e.target.value.toUpperCase())}
              placeholder="e.g. CF-2026-0001"
              required
              className="w-full pl-11 pr-4 py-3 rounded-xl border border-slate-300 font-mono text-sm focus:ring-2 focus:ring-brand-500 focus:border-brand-500 outline-none uppercase tracking-wider"
            />
          </div>

          <button
            type="submit"
            disabled={loading || !trackingNumber.trim()}
            className="w-full py-3 rounded-xl font-bold text-white bg-brand-600 hover:bg-brand-700 shadow-md shadow-brand-600/20 active:scale-95 transition-all text-sm flex items-center justify-center gap-2 disabled:opacity-50"
          >
            <span>{loading ? 'Locating Report...' : 'Track Ticket Status'}</span>
            <ArrowRight className="w-4 h-4" />
          </button>
        </form>

        {/* Quick Demo Lookups */}
        <div className="pt-4 border-t border-slate-100">
          <p className="text-xs font-semibold text-slate-500 mb-2">
            Try quick sample tracking numbers from database seed:
          </p>
          <div className="flex flex-wrap gap-2 text-xs">
            {['CF-2026-0001', 'CF-2026-0002', 'CF-2026-0003', 'CF-2026-0004', 'CF-2026-0005'].map(
              (sample) => (
                <button
                  key={sample}
                  type="button"
                  onClick={() => handleQuickLookup(sample)}
                  className="px-2.5 py-1 rounded-lg bg-slate-100 hover:bg-brand-50 hover:text-brand-700 border border-slate-200 font-mono text-[11px] font-semibold text-slate-700 transition-colors"
                >
                  {sample}
                </button>
              )
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
