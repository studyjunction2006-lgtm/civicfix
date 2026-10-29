import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { assignmentApi, issueApi } from '../services/api';
import { StatusBadge } from '../components/common/StatusBadge';
import { PriorityBadge } from '../components/common/PriorityBadge';
import { StatusTransitionModal } from '../components/issue/StatusTransitionModal';
import { Link } from 'react-router-dom';
import {
  Wrench,
  CheckCircle,
  XCircle,
  Play,
  MapPin,
  Clock,
  ExternalLink,
  Camera,
} from 'lucide-react';

export const WorkerDashboard = () => {
  const { user } = useAuth();
  const [assignments, setAssignments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedIssue, setSelectedIssue] = useState(null);
  const [resolveModalOpen, setResolveModalOpen] = useState(false);

  const fetchMyAssignments = async () => {
    setLoading(true);
    try {
      const res = await assignmentApi.getMyAssignments({ page: 0, size: 30 });
      const items = res.content || res || [];
      setAssignments(items);
    } catch (err) {
      console.error('Failed to load worker assignments:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMyAssignments();
  }, []);

  const handleRespond = async (assignmentId, action) => {
    let reason = null;
    if (action === 'DECLINE') {
      reason = prompt('Please enter the reason for declining this assignment:');
      if (!reason) return;
    }

    try {
      await assignmentApi.respondToAssignment(assignmentId, {
        action,
        declineReason: reason,
      });
      alert(`Assignment ${action.toLowerCase()}ed successfully!`);
      fetchMyAssignments();
    } catch (err) {
      alert(err.message || 'Action failed');
    }
  };

  const handleMarkInProgress = async (issueId) => {
    try {
      await issueApi.transitionStatus(issueId, {
        newStatus: 'IN_PROGRESS',
        notes: 'Technician has arrived on site and commenced repairs.',
      });
      alert('Task marked as In Progress!');
      fetchMyAssignments();
    } catch (err) {
      alert(err.message || 'Status change failed');
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Worker Header Banner */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-6 border-b border-slate-200">
        <div>
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-amber-50 text-amber-700 border border-amber-200 mb-2">
            <Wrench className="w-3.5 h-3.5 text-amber-600" />
            <span>Field Technician Operations Portal</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            My Assigned Field Tasks
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Technician: <strong className="text-slate-900">{user?.fullName || 'Ramesh Kumar'}</strong> • Department: {user?.departmentName || 'Maintenance'}
          </p>
        </div>

        <button
          onClick={fetchMyAssignments}
          className="px-4 py-2 text-xs font-semibold text-slate-700 bg-white hover:bg-slate-50 border border-slate-300 rounded-xl shadow-xs transition-colors"
        >
          Refresh Tasks
        </button>
      </div>

      {/* Task Cards Grid */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-bold text-slate-900">Active Work Orders ({assignments.length})</h2>
          <span className="text-xs text-slate-400">Accept tasks and attach proof of resolution</span>
        </div>

        {loading ? (
          <div className="py-20 text-center text-slate-400 text-xs">Loading assigned tasks...</div>
        ) : assignments.length === 0 ? (
          <div className="py-16 text-center bg-white rounded-2xl border border-slate-200 p-8 space-y-2">
            <CheckCircle className="w-10 h-10 text-emerald-500 mx-auto" />
            <h3 className="text-base font-bold text-slate-800">All assigned tasks completed!</h3>
            <p className="text-xs text-slate-500 max-w-sm mx-auto">
              You currently have no pending repairs assigned. New dispatches from your supervisor will appear here.
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {assignments.map((asg) => {
              const isPending = asg.status === 'PENDING';
              const isAccepted = asg.status === 'ACCEPTED';
              const isCompleted = asg.status === 'COMPLETED';

              return (
                <div
                  key={asg.id}
                  className="bg-white rounded-2xl border border-slate-200/90 shadow-xs p-5 space-y-4 flex flex-col justify-between hover:border-slate-300 transition-all"
                >
                  <div className="space-y-3">
                    <div className="flex items-center justify-between">
                      <span className="font-mono text-xs font-bold text-slate-700 bg-slate-100 px-2 py-0.5 rounded">
                        Task #{asg.id} • Issue #{asg.issueId}
                      </span>
                      <span
                        className={`text-xs font-semibold px-2.5 py-0.5 rounded-full ${
                          isPending
                            ? 'bg-amber-100 text-amber-800'
                            : isAccepted
                            ? 'bg-blue-100 text-blue-800'
                            : isCompleted
                            ? 'bg-emerald-100 text-emerald-800'
                            : 'bg-rose-100 text-rose-800'
                        }`}
                      >
                        Assignment: {asg.status}
                      </span>
                    </div>

                    <h3 className="text-base font-bold text-slate-900">
                      {asg.issueTitle || `Issue #${asg.issueId}`}
                    </h3>

                    <div className="text-xs text-slate-500 space-y-1">
                      <div className="flex items-center gap-1.5">
                        <MapPin className="w-3.5 h-3.5 text-slate-400" />
                        <span>{asg.addressText || 'Campus Location'}</span>
                      </div>
                      <div className="flex items-center gap-1.5">
                        <Clock className="w-3.5 h-3.5 text-slate-400" />
                        <span>Assigned by {asg.assignedByName || 'Department Supervisor'}</span>
                      </div>
                    </div>

                    {asg.workerNotes && (
                      <div className="p-2.5 bg-slate-50 rounded-xl text-xs text-slate-600 border border-slate-100">
                        <strong>Dispatch Notes:</strong> {asg.workerNotes}
                      </div>
                    )}
                  </div>

                  {/* Actions Toolbar */}
                  <div className="pt-3 border-t border-slate-100 flex flex-wrap items-center justify-between gap-2">
                    <Link
                      to={`/issues/${asg.issueId}`}
                      className="text-xs font-semibold text-brand-600 hover:text-brand-700 flex items-center gap-1"
                    >
                      View Report <ExternalLink className="w-3.5 h-3.5" />
                    </Link>

                    <div className="flex items-center gap-2">
                      {isPending && (
                        <>
                          <button
                            onClick={() => handleRespond(asg.id, 'ACCEPT')}
                            className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-semibold text-white bg-brand-600 hover:bg-brand-700 rounded-xl shadow-xs transition-colors"
                          >
                            <CheckCircle className="w-3.5 h-3.5" /> Accept
                          </button>
                          <button
                            onClick={() => handleRespond(asg.id, 'DECLINE')}
                            className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-semibold text-rose-700 bg-rose-50 hover:bg-rose-100 rounded-xl border border-rose-200 transition-colors"
                          >
                            <XCircle className="w-3.5 h-3.5" /> Decline
                          </button>
                        </>
                      )}

                      {isAccepted && (
                        <>
                          <button
                            onClick={() => handleMarkInProgress(asg.issueId)}
                            className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-semibold text-cyan-800 bg-cyan-100 hover:bg-cyan-200 rounded-xl transition-colors"
                          >
                            <Play className="w-3.5 h-3.5" /> In Progress
                          </button>
                          <button
                            onClick={() => {
                              setSelectedIssue({ id: asg.issueId, status: 'IN_PROGRESS' });
                              setResolveModalOpen(true);
                            }}
                            className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl shadow-xs transition-colors"
                          >
                            <Camera className="w-3.5 h-3.5" /> Complete & Proof
                          </button>
                        </>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Proof & Resolution Modal */}
      {selectedIssue && (
        <StatusTransitionModal
          isOpen={resolveModalOpen}
          onClose={() => setResolveModalOpen(false)}
          issue={selectedIssue}
          allowedStatuses={['RESOLVED']}
          onStatusChanged={fetchMyAssignments}
        />
      )}
    </div>
  );
};
