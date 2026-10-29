import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { issueApi } from '../services/api';
import { IssueCard } from '../components/issue/IssueCard';
import { Link } from 'react-router-dom';
import { PlusCircle, FileText, CheckCircle2, Clock, AlertCircle } from 'lucide-react';

export const CitizenDashboard = () => {
  const { user } = useAuth();
  const [myIssues, setMyIssues] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchMyIssues = async () => {
      setLoading(true);
      try {
        const res = await issueApi.getMyReports({ page: 0, size: 20 });
        const items = res.content || res || [];
        setMyIssues(items);
      } catch (err) {
        console.error('Failed to load my reports:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchMyIssues();
  }, []);

  const total = myIssues.length;
  const inProgress = myIssues.filter(
    (i) => i.status === 'IN_PROGRESS' || i.status === 'ASSIGNED' || i.status === 'UNDER_REVIEW'
  ).length;
  const resolved = myIssues.filter((i) => i.status === 'RESOLVED' || i.status === 'CLOSED').length;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Top Banner */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-6 border-b border-slate-200">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            Citizen Dashboard
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Welcome back, <span className="font-semibold text-slate-900">{user?.fullName || 'Citizen'}</span>. Track resolution status of your reported problems.
          </p>
        </div>

        <Link
          to="/report"
          className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl font-bold text-white bg-brand-600 hover:bg-brand-700 shadow-md shadow-brand-600/20 active:scale-95 transition-all text-xs sm:text-sm"
        >
          <PlusCircle className="w-4 h-4" />
          Report New Issue
        </Link>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-white p-5 rounded-2xl border border-slate-200/90 shadow-xs flex items-center gap-4">
          <div className="p-3 rounded-xl bg-blue-50 text-blue-600">
            <FileText className="w-6 h-6" />
          </div>
          <div>
            <div className="text-2xl font-extrabold text-slate-900">{total}</div>
            <div className="text-xs text-slate-500 font-medium">My Total Reports</div>
          </div>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200/90 shadow-xs flex items-center gap-4">
          <div className="p-3 rounded-xl bg-amber-50 text-amber-600">
            <Clock className="w-6 h-6" />
          </div>
          <div>
            <div className="text-2xl font-extrabold text-amber-600">{inProgress}</div>
            <div className="text-xs text-slate-500 font-medium">Under Triage & Repair</div>
          </div>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200/90 shadow-xs flex items-center gap-4">
          <div className="p-3 rounded-xl bg-emerald-50 text-emerald-600">
            <CheckCircle2 className="w-6 h-6" />
          </div>
          <div>
            <div className="text-2xl font-extrabold text-emerald-600">{resolved}</div>
            <div className="text-xs text-slate-500 font-medium">Completed & Verified</div>
          </div>
        </div>
      </div>

      {/* My Reports Grid */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-bold text-slate-900">My Reported Issues</h2>
          <span className="text-xs text-slate-400">{myIssues.length} issues found</span>
        </div>

        {loading ? (
          <div className="py-20 text-center text-slate-400">Loading your reports...</div>
        ) : myIssues.length === 0 ? (
          <div className="py-16 text-center bg-white rounded-2xl border border-slate-200 p-8 space-y-3">
            <AlertCircle className="w-10 h-10 text-slate-300 mx-auto" />
            <h3 className="text-base font-bold text-slate-800">You haven't submitted any reports yet</h3>
            <p className="text-xs text-slate-500 max-w-sm mx-auto">
              Notice a broken pipe, streetlight failure, or hazardous corridor trash? Submit a report with photo proof.
            </p>
            <Link
              to="/report"
              className="inline-flex items-center gap-1.5 px-4 py-2 text-xs font-semibold text-white bg-brand-600 rounded-xl hover:bg-brand-700 transition-colors"
            >
              Report an Issue
            </Link>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {myIssues.map((issue) => (
              <IssueCard key={issue.id} issue={issue} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
