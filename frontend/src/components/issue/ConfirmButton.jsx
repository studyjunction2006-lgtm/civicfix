import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { confirmationApi } from '../../services/api';
import { Users, Check, MessageSquare, Plus } from 'lucide-react';
import { Modal } from '../common/Modal';

export const ConfirmButton = ({ issueId, initialCount = 0, onConfirmed }) => {
  const { user, isAuthenticated } = useAuth();
  const [count, setCount] = useState(initialCount);
  const [hasConfirmed, setHasConfirmed] = useState(false);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [note, setNote] = useState('');

  useEffect(() => {
    setCount(initialCount);
  }, [initialCount]);

  useEffect(() => {
    const checkConfirmation = async () => {
      if (!isAuthenticated || !issueId) return;
      try {
        const res = await confirmationApi.checkUserConfirmed(issueId);
        setHasConfirmed(res?.confirmed || false);
      } catch (err) {
        // Soft fail
      }
    };
    checkConfirmation();
  }, [issueId, isAuthenticated]);

  const handleConfirmSubmit = async (e) => {
    e.preventDefault();
    if (!isAuthenticated) return;

    setLoading(true);
    try {
      await confirmationApi.confirmIssue(issueId, note);
      const newCount = count + 1;
      setCount(newCount);
      setHasConfirmed(true);
      setModalOpen(false);
      setNote('');
      if (onConfirmed) onConfirmed(newCount);
    } catch (err) {
      alert(err.message || 'Failed to submit confirmation');
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <div className="flex items-center gap-2">
        {hasConfirmed ? (
          <button
            disabled
            className="inline-flex items-center gap-2 px-3.5 py-2 rounded-xl text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200 cursor-default"
          >
            <Check className="w-4 h-4 text-emerald-600" />
            Confirmed ({count})
          </button>
        ) : (
          <button
            onClick={() => {
              if (!isAuthenticated) {
                alert('Please sign in or use the quick persona switcher at top to confirm this issue');
                return;
              }
              setModalOpen(true);
            }}
            className="inline-flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-semibold bg-white hover:bg-brand-50 text-brand-700 border border-brand-200 shadow-xs hover:border-brand-400 active:scale-95 transition-all"
          >
            <Plus className="w-4 h-4 text-brand-600" />
            <span>Confirm This Issue</span>
            <span className="bg-brand-100 text-brand-800 px-1.5 py-0.2 rounded-full font-mono text-[11px]">
              {count}
            </span>
          </button>
        )}
      </div>

      {/* Confirmation Note Modal */}
      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title="Confirm Community Issue"
      >
        <form onSubmit={handleConfirmSubmit} className="space-y-4">
          <p className="text-xs text-slate-600 leading-relaxed">
            By confirming this problem, you help verify its urgency and prevent duplicate submissions. High community confirmations automatically boost the priority score and alert department supervisors faster.
          </p>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Optional Note / Eyewitness Detail
            </label>
            <textarea
              value={note}
              onChange={(e) => setNote(e.target.value)}
              placeholder="e.g. Saw water leaking into the physics hallway during the 10 AM class..."
              rows={3}
              className="w-full text-xs p-3 rounded-xl border border-slate-300 focus:ring-2 focus:ring-brand-500 focus:border-brand-500 outline-none transition-all"
            />
          </div>

          <div className="flex items-center justify-end gap-2 pt-2 border-t border-slate-100">
            <button
              type="button"
              onClick={() => setModalOpen(false)}
              className="px-3 py-1.5 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-lg transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="px-4 py-2 text-xs font-semibold text-white bg-brand-600 hover:bg-brand-700 rounded-xl shadow-xs transition-colors disabled:opacity-50 flex items-center gap-1.5"
            >
              <Users className="w-3.5 h-3.5" />
              {loading ? 'Submitting...' : 'Confirm Issue (+1)'}
            </button>
          </div>
        </form>
      </Modal>
    </>
  );
};
