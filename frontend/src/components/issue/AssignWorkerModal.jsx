import React, { useState, useEffect } from 'react';
import { Modal } from '../common/Modal';
import { departmentApi, userApi, assignmentApi } from '../../services/api';
import { UserCheck, Shield } from 'lucide-react';

export const AssignWorkerModal = ({ isOpen, onClose, issue, onAssigned }) => {
  const [departments, setDepartments] = useState([]);
  const [workers, setWorkers] = useState([]);
  const [selectedDeptId, setSelectedDeptId] = useState(issue?.departmentId || '');
  const [selectedWorkerId, setSelectedWorkerId] = useState('');
  const [notes, setNotes] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen) {
      departmentApi
        .getAllDepartments()
        .then((data) => {
          setDepartments(data || []);
          if (!selectedDeptId && issue?.departmentId) {
            setSelectedDeptId(issue.departmentId);
          } else if (!selectedDeptId && data && data.length > 0) {
            setSelectedDeptId(data[0].id);
          }
        })
        .catch(console.error);
    }
  }, [isOpen, issue]);

  useEffect(() => {
    if (selectedDeptId) {
      userApi
        .getWorkers(selectedDeptId)
        .then((data) => {
          setWorkers(data || []);
          if (data && data.length > 0) {
            setSelectedWorkerId(data[0].id);
          } else {
            setSelectedWorkerId('');
          }
        })
        .catch(console.error);
    }
  }, [selectedDeptId]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!selectedWorkerId) {
      alert('Please select an available technician to assign');
      return;
    }

    setLoading(true);
    try {
      const res = await assignmentApi.assignWorker(issue.id, {
        workerId: Number(selectedWorkerId),
        departmentId: selectedDeptId ? Number(selectedDeptId) : null,
        notes,
      });
      alert('Worker assigned successfully!');
      if (onAssigned) onAssigned(res);
      onClose();
    } catch (err) {
      alert(err.message || 'Failed to assign worker');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Dispatch Issue to Department Worker">
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-xs font-semibold text-slate-700 mb-1">
            Department
          </label>
          <select
            value={selectedDeptId}
            onChange={(e) => setSelectedDeptId(e.target.value)}
            className="w-full text-xs p-2.5 rounded-xl border border-slate-300 focus:ring-2 focus:ring-brand-500 bg-white"
          >
            {departments.map((d) => (
              <option key={d.id} value={d.id}>
                {d.name} ({d.code})
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-700 mb-1">
            Assign Field Technician / Worker
          </label>
          {workers.length === 0 ? (
            <div className="text-xs text-amber-700 bg-amber-50 p-3 rounded-xl border border-amber-200">
              No workers currently registered in this department. (You can switch department or add workers in Admin console).
            </div>
          ) : (
            <select
              value={selectedWorkerId}
              onChange={(e) => setSelectedWorkerId(e.target.value)}
              className="w-full text-xs p-2.5 rounded-xl border border-slate-300 focus:ring-2 focus:ring-brand-500 bg-white"
            >
              {workers.map((w) => (
                <option key={w.id} value={w.id}>
                  {w.fullName} ({w.email})
                </option>
              ))}
            </select>
          )}
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-700 mb-1">
            Work Dispatch Instructions (Optional)
          </label>
          <textarea
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            placeholder="e.g. Bring pipe replacement sealants and safety goggles. Clear students from the area first."
            rows={3}
            className="w-full text-xs p-2.5 rounded-xl border border-slate-300 focus:ring-2 focus:ring-brand-500 outline-none"
          />
        </div>

        <div className="flex items-center justify-end gap-2 pt-2 border-t border-slate-100">
          <button
            type="button"
            onClick={onClose}
            className="px-3.5 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-lg"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={loading || !selectedWorkerId}
            className="px-4 py-2 text-xs font-semibold text-white bg-brand-600 hover:bg-brand-700 rounded-xl shadow-xs transition-colors disabled:opacity-50 flex items-center gap-1.5"
          >
            <UserCheck className="w-3.5 h-3.5" />
            {loading ? 'Dispatching...' : 'Dispatch Assignment'}
          </button>
        </div>
      </form>
    </Modal>
  );
};
