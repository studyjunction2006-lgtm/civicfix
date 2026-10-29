import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { adminApi, analyticsApi, departmentApi } from '../services/api';
import { Modal } from '../components/common/Modal';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from 'recharts';
import {
  ShieldAlert,
  Users,
  Activity,
  UserPlus,
  CheckCircle,
  XCircle,
  FileText,
  Building2,
  TrendingUp,
} from 'lucide-react';

const COLORS = ['#059669', '#2563eb', '#f59e0b', '#e11d48', '#8b5cf6', '#64748b'];

export const AdminDashboard = () => {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState('analytics'); // 'analytics', 'users', 'audit'

  // Data states
  const [analytics, setAnalytics] = useState(null);
  const [users, setUsers] = useState([]);
  const [auditLogs, setAuditLogs] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);

  // Create User Modal
  const [createUserOpen, setCreateUserOpen] = useState(false);
  const [newUser, setNewUser] = useState({
    fullName: '',
    email: '',
    password: 'password123',
    role: 'WORKER',
    departmentId: '',
    phone: '',
  });

  const loadData = async () => {
    setLoading(true);
    try {
      const [analyticsRes, usersRes, auditRes, deptRes] = await Promise.allSettled([
        analyticsApi.getAnalyticsSummary(),
        adminApi.getUsers({ page: 0, size: 50 }),
        adminApi.getAuditLogs({ page: 0, size: 50 }),
        departmentApi.getAllDepartments(),
      ]);

      if (analyticsRes.status === 'fulfilled' && analyticsRes.value) {
        setAnalytics(analyticsRes.value);
      }
      if (usersRes.status === 'fulfilled' && usersRes.value) {
        setUsers(usersRes.value.content || usersRes.value || []);
      }
      if (auditRes.status === 'fulfilled' && auditRes.value) {
        setAuditLogs(auditRes.value.content || auditRes.value || []);
      }
      if (deptRes.status === 'fulfilled' && deptRes.value) {
        setDepartments(deptRes.value || []);
        if (deptRes.value.length > 0 && !newUser.departmentId) {
          setNewUser((prev) => ({ ...prev, departmentId: deptRes.value[0].id }));
        }
      }
    } catch (err) {
      console.error('Failed to load admin data:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleRoleChange = async (userId, newRole) => {
    try {
      await adminApi.updateUserRole(userId, { role: newRole });
      alert('User role updated successfully');
      loadData();
    } catch (err) {
      alert(err.message || 'Failed to update role');
    }
  };

  const handleToggleActive = async (userId, currentActive) => {
    try {
      await adminApi.toggleUserStatus(userId, !currentActive);
      loadData();
    } catch (err) {
      alert(err.message || 'Failed to toggle status');
    }
  };

  const handleCreateUser = async (e) => {
    e.preventDefault();
    try {
      await adminApi.createUser({
        ...newUser,
        departmentId: newUser.departmentId ? Number(newUser.departmentId) : null,
      });
      alert('User created successfully!');
      setCreateUserOpen(false);
      loadData();
    } catch (err) {
      alert(err.message || 'Failed to create user');
    }
  };

  // Prepare chart data from status counts
  const statusChartData = analytics?.statusCounts
    ? Object.entries(analytics.statusCounts).map(([key, val]) => ({
        name: key.replace(/_/g, ' '),
        count: val,
      }))
    : [
        { name: 'Report Created', count: 1 },
        { name: 'Verified', count: 1 },
        { name: 'Assigned', count: 1 },
        { name: 'In Progress', count: 1 },
        { name: 'Resolved', count: 1 },
      ];

  const deptChartData = analytics?.departmentWorkloads
    ? analytics.departmentWorkloads.map((d) => ({
        name: d.departmentName,
        total: d.totalIssues,
        resolved: d.resolvedIssues,
      }))
    : [
        { name: 'Estate', total: 2, resolved: 0 },
        { name: 'Electrical', total: 1, resolved: 1 },
        { name: 'Sanitation', total: 2, resolved: 0 },
        { name: 'IT Network', total: 1, resolved: 0 },
      ];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-6 border-b border-slate-200">
        <div>
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-rose-50 text-rose-700 border border-rose-200 mb-2">
            <ShieldAlert className="w-3.5 h-3.5 text-rose-600" />
            <span>Campus Administrator & Dean Console</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            Platform Governance & Analytics
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Administrator: <strong className="text-slate-900">{user?.fullName || 'Dr. Arvind Mehra (Dean)'}</strong> • System Monitoring
          </p>
        </div>

        <button
          onClick={() => setCreateUserOpen(true)}
          className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl font-bold text-white bg-slate-900 hover:bg-slate-800 shadow-xs transition-colors text-xs"
        >
          <UserPlus className="w-4 h-4" />
          Create Staff / Worker Account
        </button>
      </div>

      {/* Tabs */}
      <div className="flex items-center gap-2 border-b border-slate-200 text-xs font-semibold">
        <button
          onClick={() => setActiveTab('analytics')}
          className={`pb-3 px-3 transition-colors border-b-2 ${
            activeTab === 'analytics'
              ? 'border-brand-600 text-brand-600'
              : 'border-transparent text-slate-500 hover:text-slate-900'
          }`}
        >
          Overview & KPI Charts
        </button>
        <button
          onClick={() => setActiveTab('users')}
          className={`pb-3 px-3 transition-colors border-b-2 ${
            activeTab === 'users'
              ? 'border-brand-600 text-brand-600'
              : 'border-transparent text-slate-500 hover:text-slate-900'
          }`}
        >
          User & Role Management ({users.length})
        </button>
        <button
          onClick={() => setActiveTab('audit')}
          className={`pb-3 px-3 transition-colors border-b-2 ${
            activeTab === 'audit'
              ? 'border-brand-600 text-brand-600'
              : 'border-transparent text-slate-500 hover:text-slate-900'
          }`}
        >
          Platform Audit Trail ({auditLogs.length})
        </button>
      </div>

      {/* Tab 1: Analytics */}
      {activeTab === 'analytics' && (
        <div className="space-y-8">
          {/* KPI Summary Cards */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="bg-white p-5 rounded-2xl border border-slate-200/90 shadow-xs text-center">
              <div className="text-2xl font-extrabold text-slate-900">
                {analytics?.totalIssues || 5}
              </div>
              <div className="text-xs text-slate-500 mt-1">Total Issues Logged</div>
            </div>

            <div className="bg-white p-5 rounded-2xl border border-slate-200/90 shadow-xs text-center">
              <div className="text-2xl font-extrabold text-emerald-600">
                {analytics?.resolvedIssues || 1}
              </div>
              <div className="text-xs text-slate-500 mt-1">Resolved & Verified</div>
            </div>

            <div className="bg-white p-5 rounded-2xl border border-slate-200/90 shadow-xs text-center">
              <div className="text-2xl font-extrabold text-indigo-600">
                {Math.round(analytics?.resolutionRate || 20)}%
              </div>
              <div className="text-xs text-slate-500 mt-1">Resolution Rate</div>
            </div>

            <div className="bg-white p-5 rounded-2xl border border-slate-200/90 shadow-xs text-center">
              <div className="text-2xl font-extrabold text-amber-600">
                {Math.round(analytics?.averageResolutionHours || 18)}h
              </div>
              <div className="text-xs text-slate-500 mt-1">Avg Resolution Time</div>
            </div>
          </div>

          {/* Charts Row */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Status Breakdown Bar Chart */}
            <div className="bg-white p-6 rounded-2xl border border-slate-200/90 shadow-xs space-y-4">
              <h3 className="text-sm font-bold text-slate-900">Issues by Lifecycle Stage</h3>
              <div className="h-64 w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={statusChartData}>
                    <XAxis dataKey="name" tick={{ fontSize: 10 }} />
                    <YAxis allowDecimals={false} tick={{ fontSize: 10 }} />
                    <Tooltip />
                    <Bar dataKey="count" fill="#059669" radius={[6, 6, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>

            {/* Department Workload Breakdown */}
            <div className="bg-white p-6 rounded-2xl border border-slate-200/90 shadow-xs space-y-4">
              <h3 className="text-sm font-bold text-slate-900">Department Workload Distribution</h3>
              <div className="h-64 w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={deptChartData}>
                    <XAxis dataKey="name" tick={{ fontSize: 10 }} />
                    <YAxis allowDecimals={false} tick={{ fontSize: 10 }} />
                    <Tooltip />
                    <Bar dataKey="total" fill="#3b82f6" name="Total Tickets" radius={[4, 4, 0, 0]} />
                    <Bar dataKey="resolved" fill="#10b981" name="Resolved" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Tab 2: Users Management */}
      {activeTab === 'users' && (
        <div className="bg-white rounded-2xl border border-slate-200/90 shadow-xs overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 text-slate-500 uppercase tracking-wider font-semibold text-[10px]">
                  <th className="py-3.5 px-4">Name & Email</th>
                  <th className="py-3.5 px-4">Role</th>
                  <th className="py-3.5 px-4">Department</th>
                  <th className="py-3.5 px-4">Status</th>
                  <th className="py-3.5 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {users.map((u) => (
                  <tr key={u.id} className="hover:bg-slate-50/80 transition-colors">
                    <td className="py-3 px-4">
                      <div className="font-bold text-slate-900">{u.fullName}</div>
                      <div className="text-[11px] text-slate-400 font-mono">{u.email}</div>
                    </td>

                    <td className="py-3 px-4">
                      <select
                        value={u.role}
                        onChange={(e) => handleRoleChange(u.id, e.target.value)}
                        className="p-1 rounded-lg border border-slate-200 text-xs font-semibold bg-white"
                      >
                        <option value="CITIZEN">CITIZEN</option>
                        <option value="AUTHORITY">AUTHORITY</option>
                        <option value="WORKER">WORKER</option>
                        <option value="ADMIN">ADMIN</option>
                      </select>
                    </td>

                    <td className="py-3 px-4 text-slate-600">
                      {u.departmentName || 'None'}
                    </td>

                    <td className="py-3 px-4">
                      <span
                        className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                          u.isActive
                            ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                            : 'bg-rose-50 text-rose-700 border border-rose-200'
                        }`}
                      >
                        {u.isActive ? 'Active' : 'Suspended'}
                      </span>
                    </td>

                    <td className="py-3 px-4 text-right">
                      <button
                        onClick={() => handleToggleActive(u.id, u.isActive)}
                        className={`px-2.5 py-1 rounded-lg text-xs font-semibold transition-colors ${
                          u.isActive
                            ? 'text-rose-600 hover:bg-rose-50'
                            : 'text-emerald-600 hover:bg-emerald-50'
                        }`}
                      >
                        {u.isActive ? 'Deactivate' : 'Activate'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Tab 3: Platform Audit Trail */}
      {activeTab === 'audit' && (
        <div className="bg-white rounded-2xl border border-slate-200/90 shadow-xs overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 text-slate-500 uppercase tracking-wider font-semibold text-[10px]">
                  <th className="py-3.5 px-4">Timestamp</th>
                  <th className="py-3.5 px-4">Action</th>
                  <th className="py-3.5 px-4">Actor</th>
                  <th className="py-3.5 px-4">Entity Type / ID</th>
                  <th className="py-3.5 px-4">Details</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 font-mono text-[11px]">
                {auditLogs.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="py-8 text-center text-slate-400">
                      No audit events recorded yet.
                    </td>
                  </tr>
                ) : (
                  auditLogs.map((log) => (
                    <tr key={log.id} className="hover:bg-slate-50/80">
                      <td className="py-2.5 px-4 text-slate-500">
                        {log.createdAt ? new Date(log.createdAt).toLocaleString() : ''}
                      </td>
                      <td className="py-2.5 px-4 font-bold text-slate-900">{log.action}</td>
                      <td className="py-2.5 px-4 text-slate-700">
                        {log.actorEmail} ({log.actorRole})
                      </td>
                      <td className="py-2.5 px-4 text-slate-600">
                        {log.entityType} #{log.entityId}
                      </td>
                      <td className="py-2.5 px-4 text-slate-500 max-w-xs truncate">
                        {log.details || '-'}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Create User Modal */}
      <Modal isOpen={createUserOpen} onClose={() => setCreateUserOpen(false)} title="Register Platform Account">
        <form onSubmit={handleCreateUser} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Full Name</label>
            <input
              type="text"
              value={newUser.fullName}
              onChange={(e) => setNewUser({ ...newUser, fullName: e.target.value })}
              placeholder="e.g. Suman Sen"
              required
              className="w-full text-xs p-2.5 rounded-xl border border-slate-300 outline-none focus:ring-2 focus:ring-brand-500"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Email Address</label>
            <input
              type="email"
              value={newUser.email}
              onChange={(e) => setNewUser({ ...newUser, email: e.target.value })}
              placeholder="e.g. suman@civicfix.org"
              required
              className="w-full text-xs p-2.5 rounded-xl border border-slate-300 outline-none focus:ring-2 focus:ring-brand-500"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Role</label>
              <select
                value={newUser.role}
                onChange={(e) => setNewUser({ ...newUser, role: e.target.value })}
                className="w-full text-xs p-2.5 rounded-xl border border-slate-300 bg-white"
              >
                <option value="WORKER">Worker / Technician</option>
                <option value="AUTHORITY">Authority / Supervisor</option>
                <option value="ADMIN">Dean / Administrator</option>
                <option value="CITIZEN">Citizen</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Department</label>
              <select
                value={newUser.departmentId}
                onChange={(e) => setNewUser({ ...newUser, departmentId: e.target.value })}
                className="w-full text-xs p-2.5 rounded-xl border border-slate-300 bg-white"
              >
                <option value="">None / All</option>
                {departments.map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="flex items-center justify-end gap-2 pt-2 border-t border-slate-100">
            <button
              type="button"
              onClick={() => setCreateUserOpen(false)}
              className="px-3.5 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-lg"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 text-xs font-semibold text-white bg-brand-600 hover:bg-brand-700 rounded-xl shadow-xs transition-colors"
            >
              Create Account
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
