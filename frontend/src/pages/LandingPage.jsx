import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { analyticsApi, issueApi } from '../services/api';
import { IssueMap } from '../components/map/IssueMap';
import { IssueCard } from '../components/issue/IssueCard';
import {
  ShieldCheck,
  PlusCircle,
  MapPin,
  CheckCircle,
  Clock,
  TrendingUp,
  Users,
  Search,
  Sparkles,
  ArrowRight,
  Zap,
} from 'lucide-react';

export const LandingPage = () => {
  const [stats, setStats] = useState({
    totalIssues: 5,
    resolvedIssues: 1,
    resolutionRate: 20.0,
    averageResolutionHours: 18.5,
  });
  const [mapIssues, setMapIssues] = useState([]);
  const [recentIssues, setRecentIssues] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadData = async () => {
      try {
        const [statsData, mapData, recentData] = await Promise.allSettled([
          analyticsApi.getPublicStats(),
          issueApi.getPublicMapIssues(),
          issueApi.searchIssues({ page: 0, size: 3, sortBy: 'createdAt', sortDir: 'DESC' }),
        ]);

        if (statsData.status === 'fulfilled' && statsData.value) {
          setStats(statsData.value);
        }
        if (mapData.status === 'fulfilled' && mapData.value) {
          setMapIssues(mapData.value);
        }
        if (recentData.status === 'fulfilled' && recentData.value) {
          setRecentIssues(recentData.value.content || recentData.value || []);
        }
      } catch (err) {
        console.error('Failed to load landing data:', err);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, []);

  return (
    <div className="space-y-16 pb-16">
      {/* Hero Section */}
      <section className="relative overflow-hidden pt-12 pb-16 lg:pt-20 lg:pb-24 bg-gradient-to-b from-brand-50/50 via-white to-slate-50 border-b border-slate-200/60">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto space-y-6">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold bg-brand-100 text-brand-800 border border-brand-200 shadow-xs">
              <Sparkles className="w-3.5 h-3.5 text-brand-600" />
              <span>Next-Gen Community Infrastructure Transparency</span>
            </div>

            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold text-slate-900 tracking-tight leading-tight">
              Report Campus Issues. <br className="hidden sm:inline" />
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-brand-600 to-emerald-500">
                Track Live Repairs.
              </span>
            </h1>

            <p className="text-base sm:text-lg text-slate-600 leading-relaxed font-normal">
              CivicFix connects students, faculty, and campus maintenance with real-time GPS pinpointing, community duplicate prevention, algorithmic urgency scoring, and transparent resolution audits.
            </p>

            {/* Call to action buttons */}
            <div className="flex flex-wrap items-center justify-center gap-3 pt-2">
              <Link
                to="/report"
                className="inline-flex items-center gap-2 px-6 py-3.5 rounded-xl font-bold text-white bg-brand-600 hover:bg-brand-700 shadow-lg shadow-brand-600/25 active:scale-95 transition-all text-sm"
              >
                <PlusCircle className="w-5 h-5" />
                Report an Issue Now
              </Link>

              <Link
                to="/explore"
                className="inline-flex items-center gap-2 px-6 py-3.5 rounded-xl font-bold text-slate-700 bg-white hover:bg-slate-50 border border-slate-300 shadow-xs active:scale-95 transition-all text-sm"
              >
                <MapPin className="w-5 h-5 text-brand-600" />
                Explore Public Map
              </Link>
            </div>
          </div>

          {/* KPI Statistics Counter Cards */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-12 max-w-4xl mx-auto">
            <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-xs text-center">
              <div className="text-2xl sm:text-3xl font-extrabold text-slate-900">
                {stats.totalIssues || 5}
              </div>
              <div className="text-xs font-medium text-slate-500 mt-1">Total Reported</div>
            </div>

            <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-xs text-center">
              <div className="text-2xl sm:text-3xl font-extrabold text-brand-600">
                {stats.resolvedIssues || 1}
              </div>
              <div className="text-xs font-medium text-slate-500 mt-1">Repairs Completed</div>
            </div>

            <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-xs text-center">
              <div className="text-2xl sm:text-3xl font-extrabold text-indigo-600">
                {Math.round(stats.resolutionRate || 20)}%
              </div>
              <div className="text-xs font-medium text-slate-500 mt-1">Resolution Rate</div>
            </div>

            <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-xs text-center">
              <div className="text-2xl sm:text-3xl font-extrabold text-amber-600">
                {Math.round(stats.averageResolutionHours || 18)}h
              </div>
              <div className="text-xs font-medium text-slate-500 mt-1">Avg Resolution Time</div>
            </div>
          </div>
        </div>
      </section>

      {/* Live Map Preview Section */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 mb-6">
          <div>
            <div className="flex items-center gap-2 text-xs font-bold text-brand-600 uppercase tracking-wider">
              <MapPin className="w-4 h-4" />
              Interactive Geo-Spatial Map
            </div>
            <h2 className="text-2xl font-extrabold text-slate-900 tracking-tight mt-1">
              Active Campus Maintenance Issues
            </h2>
            <p className="text-xs sm:text-sm text-slate-500 mt-0.5">
              Click markers to view status, photographs, and corroboration votes.
            </p>
          </div>

          <Link
            to="/explore"
            className="inline-flex items-center gap-1.5 text-xs font-bold text-brand-600 hover:text-brand-700 bg-brand-50 px-3 py-2 rounded-xl border border-brand-200/80 hover:bg-brand-100 transition-colors"
          >
            Open Fullscreen Map <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        <IssueMap issues={mapIssues} height="440px" />
      </section>

      {/* Feature Pillar Highlights */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-2xl mx-auto mb-12">
          <h2 className="text-xs font-bold text-brand-600 uppercase tracking-wider">
            Why CivicFix Works
          </h2>
          <h3 className="text-2xl sm:text-3xl font-extrabold text-slate-900 mt-1">
            Engineered to Solve Campus Frustration
          </h3>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-xs hover:border-brand-500/50 hover:shadow-md transition-all">
            <div className="w-10 h-10 rounded-xl bg-brand-50 text-brand-600 flex items-center justify-center mb-4">
              <Users className="w-5 h-5" />
            </div>
            <h4 className="text-base font-bold text-slate-900 mb-2">Community Confirmations</h4>
            <p className="text-xs text-slate-500 leading-relaxed">
              Instead of 50 people submitting separate reports for one burst pipe, citizens "+1 Confirm" existing reports. Confirmations boost algorithmic priority and alert department heads faster.
            </p>
          </div>

          <div className="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-xs hover:border-brand-500/50 hover:shadow-md transition-all">
            <div className="w-10 h-10 rounded-xl bg-indigo-50 text-indigo-600 flex items-center justify-center mb-4">
              <Zap className="w-5 h-5" />
            </div>
            <h4 className="text-base font-bold text-slate-900 mb-2">Algorithmic SLA Scoring</h4>
            <p className="text-xs text-slate-500 leading-relaxed">
              Issues receive transparent priority scores calculated from severity, confirmations, age, and department SLA. Critical water and electrical hazards bypass bottlenecks automatically.
            </p>
          </div>

          <div className="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-xs hover:border-brand-500/50 hover:shadow-md transition-all">
            <div className="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center mb-4">
              <CheckCircle className="w-5 h-5" />
            </div>
            <h4 className="text-base font-bold text-slate-900 mb-2">Proof-of-Fix & Citizen Review</h4>
            <p className="text-xs text-slate-500 leading-relaxed">
              Workers submit before/after photo evidence upon completion. Citizens who reported or confirmed the issue verify the repair and rate satisfaction (1-5 stars) before closing.
            </p>
          </div>
        </div>
      </section>

      {/* Recent Issues Showcase */}
      {recentIssues.length > 0 && (
        <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h3 className="text-xl font-bold text-slate-900">Recently Reported</h3>
              <p className="text-xs text-slate-500">Issues undergoing review and repair</p>
            </div>
            <Link
              to="/explore"
              className="text-xs font-semibold text-brand-600 hover:text-brand-700 flex items-center gap-1"
            >
              View all <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {recentIssues.map((issue) => (
              <IssueCard key={issue.id} issue={issue} />
            ))}
          </div>
        </section>
      )}
    </div>
  );
};
