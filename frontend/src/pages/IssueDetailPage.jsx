import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { issueApi, confirmationApi } from '../services/api';
import { StatusBadge } from '../components/common/StatusBadge';
import { PriorityBadge, SeverityBadge } from '../components/common/PriorityBadge';
import { TimelineStepper } from '../components/issue/TimelineStepper';
import { ConfirmButton } from '../components/issue/ConfirmButton';
import { RatingModal } from '../components/issue/RatingModal';
import { AssignWorkerModal } from '../components/issue/AssignWorkerModal';
import { StatusTransitionModal } from '../components/issue/StatusTransitionModal';
import { IssueMap } from '../components/map/IssueMap';
import {
  MapPin,
  Clock,
  User,
  Users,
  Copy,
  Check,
  Shield,
  ArrowLeft,
  Star,
  Wrench,
  Sparkles,
  ChevronRight,
  Sliders,
} from 'lucide-react';

export const IssueDetailPage = () => {
  const { id } = useParams();
  const { user, isAuthenticated, hasRole } = useAuth();

  const [issue, setIssue] = useState(null);
  const [confirmations, setConfirmations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [copied, setCopied] = useState(false);

  // Modals
  const [statusModalOpen, setStatusModalOpen] = useState(false);
  const [assignModalOpen, setAssignModalOpen] = useState(false);
  const [ratingModalOpen, setRatingModalOpen] = useState(false);

  const fetchIssueDetails = async () => {
    setLoading(true);
    try {
      const data = await issueApi.getIssueById(id);
      setIssue(data);

      // Also fetch corroborating community confirmations
      confirmationApi
        .getConfirmations(id)
        .then(setConfirmations)
        .catch(console.error);
    } catch (err) {
      console.error('Failed to load issue details:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchIssueDetails();
  }, [id]);

  const handleCopyTrackingNumber = () => {
    if (!issue?.trackingNumber) return;
    navigator.clipboard.writeText(issue.trackingNumber);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  if (loading) {
    return (
      <div className="max-w-5xl mx-auto px-4 py-20 text-center text-slate-400">
        Loading issue report and audit history...
      </div>
    );
  }

  if (!issue) {
    return (
      <div className="max-w-5xl mx-auto px-4 py-20 text-center">
        <h2 className="text-xl font-bold text-slate-800">Issue Not Found</h2>
        <p className="text-xs text-slate-500 mt-2">
          The requested issue ID does not exist or may have been removed.
        </p>
        <Link
          to="/explore"
          className="inline-flex items-center gap-1.5 mt-4 text-xs font-semibold text-brand-600 hover:underline"
        >
          <ArrowLeft className="w-3.5 h-3.5" /> Return to Explore
        </Link>
      </div>
    );
  }

  // Calculate allowed statuses based on current user role & issue status
  const getAllowedStatuses = () => {
    if (!user) return [];
    if (user.role === 'ADMIN' || user.role === 'AUTHORITY') {
      return ['UNDER_REVIEW', 'VERIFIED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'REJECTED', 'CLOSED'];
    }
    if (user.role === 'WORKER') {
      return ['ACCEPTED', 'IN_PROGRESS', 'RESOLVED'];
    }
    if (user.role === 'CITIZEN' && issue.status === 'RESOLVED') {
      return ['CITIZEN_VERIFICATION', 'REOPENED'];
    }
    return [];
  };

  const allowedStatuses = getAllowedStatuses();

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Top Breadcrumb & Actions Bar */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <Link
          to="/explore"
          className="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-500 hover:text-slate-900 transition-colors"
        >
          <ArrowLeft className="w-4 h-4" /> Back to all issues
        </Link>

        {/* Action Buttons Bar based on role */}
        <div className="flex items-center gap-2 flex-wrap">
          {/* Authority / Admin: Assign Worker */}
          {(hasRole('AUTHORITY') || hasRole('ADMIN')) && (
            <button
              onClick={() => setAssignModalOpen(true)}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 rounded-xl shadow-xs transition-colors"
            >
              <Wrench className="w-3.5 h-3.5" />
              Assign Worker
            </button>
          )}

          {/* Any user with allowed status transitions */}
          {allowedStatuses.length > 0 && (
            <button
              onClick={() => setStatusModalOpen(true)}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-white bg-brand-600 hover:bg-brand-700 rounded-xl shadow-xs transition-colors"
            >
              <Sliders className="w-3.5 h-3.5" />
              Update Status
            </button>
          )}

          {/* Citizen: Rate Resolution if resolved */}
          {issue.status === 'RESOLVED' && (
            <button
              onClick={() => setRatingModalOpen(true)}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-amber-800 bg-amber-100 hover:bg-amber-200 rounded-xl border border-amber-300 shadow-xs transition-colors"
            >
              <Star className="w-3.5 h-3.5 fill-amber-500 text-amber-500" />
              Rate Repair Quality
            </button>
          )}
        </div>
      </div>

      {/* Main Issue Header Card */}
      <div className="bg-white rounded-2xl border border-slate-200/90 shadow-xs p-6 space-y-4">
        <div className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-100 pb-4">
          <div className="flex items-center gap-2">
            <span className="font-mono text-sm font-bold text-slate-700 bg-slate-100 px-2.5 py-1 rounded-lg">
              {issue.trackingNumber}
            </span>
            <button
              onClick={handleCopyTrackingNumber}
              className="p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-50 rounded-lg transition-colors"
              title="Copy tracking number"
            >
              {copied ? <Check className="w-4 h-4 text-emerald-600" /> : <Copy className="w-4 h-4" />}
            </button>
          </div>

          <div className="flex items-center gap-2 flex-wrap">
            <SeverityBadge severity={issue.severity} />
            <PriorityBadge
              priority={issue.calculatedPriority || issue.severity}
              score={issue.priorityScore}
            />
            <StatusBadge status={issue.status} size="lg" />
          </div>
        </div>

        <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight leading-snug">
          {issue.title}
        </h1>

        <div className="flex flex-wrap items-center gap-4 text-xs text-slate-500">
          <span className="flex items-center gap-1">
            <User className="w-3.5 h-3.5 text-slate-400" />
            Reported by: <strong>{issue.reporter?.fullName || 'Citizen'}</strong>
          </span>
          <span>•</span>
          <span className="flex items-center gap-1">
            <Clock className="w-3.5 h-3.5 text-slate-400" />
            {issue.createdAt ? new Date(issue.createdAt).toLocaleString() : 'N/A'}
          </span>
          {issue.departmentName && (
            <>
              <span>•</span>
              <span className="text-brand-700 font-medium bg-brand-50 px-2 py-0.5 rounded-md">
                Dept: {issue.departmentName}
              </span>
            </>
          )}
        </div>

        <p className="text-sm text-slate-700 leading-relaxed whitespace-pre-line pt-2">
          {issue.description}
        </p>

        {/* Priority Score Bar */}
        <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200/80 flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs">
          <div>
            <span className="font-bold text-slate-900">Urgency Priority Score: </span>
            <span className="font-mono font-bold text-brand-700">{Math.round(issue.priorityScore || 0)} / 100</span>
            <span className="text-slate-500 ml-2">
              (Calculated from severity, category SLA, age, and {issue.confirmationCount} corroborations)
            </span>
          </div>

          {/* Confirm Button */}
          <ConfirmButton
            issueId={issue.id}
            initialCount={issue.confirmationCount}
            onConfirmed={(count) => setIssue({ ...issue, confirmationCount: count })}
          />
        </div>
      </div>

      {/* Grid: Photos & Physical Location */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 items-start">
        {/* Photos Card */}
        <div className="bg-white rounded-2xl border border-slate-200/90 shadow-xs p-6 space-y-4">
          <h3 className="text-sm font-bold text-slate-900">Photographic Evidence</h3>

          {issue.images && issue.images.length > 0 ? (
            <div className="grid grid-cols-2 gap-3">
              {issue.images.map((img) => (
                <div
                  key={img.id}
                  className="relative h-44 rounded-xl overflow-hidden border border-slate-200 bg-slate-100 group"
                >
                  <img
                    src={img.imageUrl}
                    alt="Issue evidence"
                    className="w-full h-full object-cover group-hover:scale-105 transition-transform"
                  />
                  <div className="absolute bottom-1.5 left-1.5 bg-navy-950/70 text-white text-[10px] font-semibold px-2 py-0.5 rounded backdrop-blur-xs">
                    {img.imageType?.replace(/_/g, ' ') || 'Initial Report'}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="p-8 text-center bg-slate-50 rounded-xl text-xs text-slate-400">
              No photographs attached to this report.
            </div>
          )}
        </div>

        {/* Location & Map Card */}
        <div className="bg-white rounded-2xl border border-slate-200/90 shadow-xs p-6 space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-bold text-slate-900">Physical Campus Location</h3>
            <span className="text-xs text-slate-500 font-mono">
              {Number(issue.latitude)?.toFixed(4)}, {Number(issue.longitude)?.toFixed(4)}
            </span>
          </div>

          <div className="flex items-center gap-1.5 text-xs text-slate-600 bg-slate-50 p-2.5 rounded-xl border border-slate-100">
            <MapPin className="w-4 h-4 text-brand-600 shrink-0" />
            <span className="font-medium">{issue.addressText || 'Campus Grounds'}</span>
          </div>

          <div className="rounded-xl overflow-hidden border border-slate-200">
            <IssueMap
              issues={[issue]}
              center={[parseFloat(issue.latitude), parseFloat(issue.longitude)]}
              zoom={17}
              height="260px"
            />
          </div>
        </div>
      </div>

      {/* Grid: Assigned Worker & Community Confirmations */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 items-start">
        {/* Worker Assignment Card */}
        <div className="bg-white rounded-2xl border border-slate-200/90 shadow-xs p-6 space-y-3">
          <h3 className="text-sm font-bold text-slate-900">Dispatched Field Technician</h3>

          {issue.assignedWorker ? (
            <div className="flex items-center gap-3 p-3 bg-slate-50 rounded-xl border border-slate-200">
              <div className="w-10 h-10 rounded-xl bg-amber-600 text-white flex items-center justify-center font-bold text-sm">
                {issue.assignedWorker.fullName.charAt(0)}
              </div>
              <div className="flex-1 min-w-0">
                <div className="text-xs font-bold text-slate-900 truncate">
                  {issue.assignedWorker.fullName}
                </div>
                <div className="text-[11px] text-slate-500 truncate">
                  {issue.assignedWorker.email}
                </div>
                <div className="text-[10px] text-amber-700 font-semibold mt-0.5">
                  Role: Field Technician / Worker
                </div>
              </div>
            </div>
          ) : (
            <div className="p-6 text-center bg-slate-50 rounded-xl text-xs text-slate-400">
              No worker currently assigned. Waiting for department supervisor triage.
            </div>
          )}
        </div>

        {/* Community Corroborations Card */}
        <div className="bg-white rounded-2xl border border-slate-200/90 shadow-xs p-6 space-y-3">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-bold text-slate-900">
              Community Confirmations ({confirmations.length})
            </h3>
            <span className="text-xs text-slate-400">Prevents duplicate tickets</span>
          </div>

          {confirmations.length === 0 ? (
            <div className="p-6 text-center bg-slate-50 rounded-xl text-xs text-slate-400">
              Be the first citizen to confirm seeing this problem!
            </div>
          ) : (
            <div className="space-y-2 max-h-48 overflow-y-auto pr-1">
              {confirmations.map((c) => (
                <div key={c.id} className="p-2.5 bg-slate-50 rounded-xl border border-slate-100 text-xs">
                  <div className="flex items-center justify-between text-[11px] text-slate-500 mb-1">
                    <span className="font-semibold text-slate-900">{c.citizenName}</span>
                    <span>{c.createdAt ? new Date(c.createdAt).toLocaleDateString() : ''}</span>
                  </div>
                  {c.note && <p className="text-slate-600 italic">"{c.note}"</p>}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Audit History Timeline */}
      <div className="bg-white rounded-2xl border border-slate-200/90 shadow-xs p-6 space-y-6">
        <div>
          <h3 className="text-base font-bold text-slate-900">Transparent Audit & Status Trail</h3>
          <p className="text-xs text-slate-500 mt-0.5">
            Every inspection, worker dispatch, and repair milestone is logged immutably.
          </p>
        </div>

        <TimelineStepper history={issue.timeline} />
      </div>

      {/* Modals */}
      <StatusTransitionModal
        isOpen={statusModalOpen}
        onClose={() => setStatusModalOpen(false)}
        issue={issue}
        allowedStatuses={allowedStatuses}
        onStatusChanged={fetchIssueDetails}
      />

      <AssignWorkerModal
        isOpen={assignModalOpen}
        onClose={() => setAssignModalOpen(false)}
        issue={issue}
        onAssigned={fetchIssueDetails}
      />

      <RatingModal
        isOpen={ratingModalOpen}
        onClose={() => setRatingModalOpen(false)}
        issueId={issue.id}
      />
    </div>
  );
};
