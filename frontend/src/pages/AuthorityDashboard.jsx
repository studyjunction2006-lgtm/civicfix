import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { issueApi, departmentApi } from '../services/api';
import { StatusBadge } from '../components/common/StatusBadge';
import { PriorityBadge } from '../components/common/PriorityBadge';
import { AssignWorkerModal } from '../components/issue/AssignWorkerModal';
import { StatusTransitionModal } from '../components/issue/StatusTransitionModal';
import { Link } from 'react-router-dom';
import {
  ShieldCheck,
  UserCheck,
  Clock,
  AlertTriangle,
  Sliders,
  ExternalLink,
  Filter,
} from 'lucide-react';

export const AuthorityDashboard = () => {
  const { user } = useAuth();
  const [issues, setIssues] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filterUnassigned, setFilterUnassigned] = useState(false);

  // Selected issue for action modals
  const [selectedIssue, setSelectedIssue] = useState(null);
  const [assignModalOpen, setAssignModalOpen] = useState(false);
  const [statusModalOpen, setStatusModalOpen] = useState(false);

  const fetchDepartmentIssues = async () => {
    setLoading(true);
    try {
      const params = {
        departmentId: user?.departmentId || undefined,
        page: 0,
        size: 50,
        sortBy: 'priorityScore',
        sortDir: 'DESC',
      };
      const res = await issueApi.searchIssues(params);
      const items = res.content || res || [];
      setIssues(items);
    } catch (err) {
      console.error('Failed to load department issues:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDepartmentIssues();
  }, [user?.departmentId]);

  const displayedIssues = filterUnassigned
    ? issues.filter((i) => !i.assignedWorker)
    : issues;

  const total = issues.length;
  const unassignedCount = issues.filter((i) => !i.assignedWorker).length;
  const criticalCount = issues.filter(
    (i) => i.calculatedPriority === 'CRITICAL' || i.calculatedPriority === 'HIGH'
  ).length;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-6 border-b border-slate-200">
        <div>
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-indigo-50 text-indigo-700 border border-indigo-200 mb-2">
            <ShieldCheck className="w-3.5 h-3.5 text-indigo-600" />
            <span>Authority & Department Supervisor Portal</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            Department Triage & Dispatch Console
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Supervisor: <strong className="text-slate-900">{user?.fullName || 'Er. Shalini Verma'}</strong> • Dept: {user?.departmentName || 'Campus Operations'}
          </p>
        </div>

        <button
          onClick={fetchDepartmentIssues}
          className="px-4 py-2 text-xs font-semibold text-slate-700 bg-white hover:bg-slate-50 border border-slate-300 rounded-xl shadow-xs transition-colors"
        >
          Refresh Triage Queue
        </button>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-white p-5 rounded-2xl border border-slate-200/90 shadow-xs">
          <div className="text-2xl font-extrabold text-slate-900">{total}</div>
          <div className="text-xs text-slate-500 font-medium mt-1">Total Department Tickets</div>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200/90 shadow-xs">
          <div className="text-2xl font-extrabold text-amber-600">{unassignedCount}</div>
          <div className="text-xs text-slate-500 font-medium mt-1">Unassigned (Awaiting Worker Dispatch)</div>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200/90 shadow-xs">
          <div className="text-2xl font-extrabold text-rose-600">{criticalCount}</div>
          <div className="text-xs text-slate-500 font-medium mt-1">High & Critical SLA Alerts</div>
        </div>
      </div>

      {/* Filter Tabs */}
      <div className="flex items-center justify-between gap-4">
        <div className="flex items-center gap-2">
          <button
            onClick={() => setFilterUnassigned(false)}
            className={`px-3.5 py-1.5 rounded-xl text-xs font-semibold transition-all ${
              !filterUnassigned
                ? 'bg-slate-900 text-white shadow-xs'
                : 'bg-white text-slate-600 border border-slate-200 hover:bg-slate-50'
            }`}
          >
            All Tickets ({total})
          </button>
          <button
            onClick={() => setFilterUnassigned(true)}
            className={`px-3.5 py-1.5 rounded-xl text-xs font-semibold transition-all ${
              filterUnassigned
                ? 'bg-amber-600 text-white shadow-xs'
                : 'bg-white text-slate-600 border border-slate-200 hover:bg-slate-50'
            }`}
          >
            Unassigned Only ({unassignedCount})
          </button>
        </div>

        <span className="text-xs text-slate-400">
          Sorted by calculated algorithmic priority
        </span>
      </div>

      {/* Issues Table */}
      <div className="bg-white rounded-2xl border border-slate-200/90 shadow-xs overflow-hidden">
        {loading ? (
          <div className="p-16 text-center text-slate-400 text-xs">Loading department tickets...</div>
        ) : displayedIssues.length === 0 ? (
          <div className="p-16 text-center text-slate-500 text-xs">
            No issues found in this triage queue.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 text-slate-500 uppercase tracking-wider font-semibold text-[10px]">
                  <th className="py-3.5 px-4">Tracking #</th>
                  <th className="py-3.5 px-4">Issue Title & Category</th>
                  <th className="py-3.5 px-4">Priority / Score</th>
                  <th className="py-3.5 px-4">Status</th>
                  <th className="py-3.5 px-4">Assigned Worker</th>
                  <th className="py-3.5 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {displayedIssues.map((issue) => (
                  <tr key={issue.id} className="hover:bg-slate-50/80 transition-colors">
                    <td className="py-3 px-4 font-mono font-bold text-slate-800">
                      {issue.trackingNumber}
                    </td>

                    <td className="py-3 px-4 max-w-xs">
                      <div className="font-bold text-slate-900 truncate">{issue.title}</div>
                      <div className="text-[11px] text-slate-400 truncate">
                        {issue.categoryName} • {issue.addressText || 'Campus'}
                      </div>
                    </td>

                    <td className="py-3 px-4">
                      <PriorityBadge
                        priority={issue.calculatedPriority || issue.severity}
                        score={issue.priorityScore}
                      />
                    </td>

                    <td className="py-3 px-4">
                      <StatusBadge status={issue.status} />
                    </td>

                    <td className="py-3 px-4">
                      {issue.assignedWorker ? (
                        <span className="font-medium text-slate-800">
                          {issue.assignedWorker.fullName}
                        </span>
                      ) : (
                        <span className="text-amber-600 font-semibold bg-amber-50 px-2 py-0.5 rounded-full border border-amber-200">
                          Unassigned
                        </span>
                      )}
                    </td>

                    <td className="py-3 px-4 text-right">
                      <div className="inline-flex items-center gap-1.5 justify-end">
                        <button
                          onClick={() => {
                            setSelectedIssue(issue);
                            setAssignModalOpen(true);
                          }}
                          className="px-2.5 py-1 bg-indigo-50 text-indigo-700 hover:bg-indigo-100 font-semibold rounded-lg border border-indigo-200 transition-colors"
                          title="Dispatch Technician"
                        >
                          Dispatch
                        </button>

                        <button
                          onClick={() => {
                            setSelectedIssue(issue);
                            setStatusModalOpen(true);
                          }}
                          className="px-2.5 py-1 bg-slate-100 text-slate-700 hover:bg-slate-200 font-semibold rounded-lg transition-colors"
                          title="Change Status"
                        >
                          Status
                        </button>

                        <Link
                          to={`/issues/${issue.id}`}
                          className="p-1 text-slate-400 hover:text-brand-600 rounded-lg hover:bg-brand-50"
                          title="Open Details"
                        >
                          <ExternalLink className="w-4 h-4" />
                        </Link>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Action Modals */}
      {selectedIssue && (
        <>
          <AssignWorkerModal
            isOpen={assignModalOpen}
            onClose={() => setAssignModalOpen(false)}
            issue={selectedIssue}
            onAssigned={fetchDepartmentIssues}
          />
          <StatusTransitionModal
            isOpen={statusModalOpen}
            onClose={() => setStatusModalOpen(false)}
            issue={selectedIssue}
            allowedStatuses={['UNDER_REVIEW', 'VERIFIED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'REJECTED', 'CLOSED']}
            onStatusChanged={fetchDepartmentIssues}
          />
        </>
      )}
    </div>
  );
};
