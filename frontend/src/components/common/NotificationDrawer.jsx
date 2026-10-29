import React from 'react';
import { useNotifications } from '../../context/NotificationContext';
import { useNavigate } from 'react-router-dom';
import { X, Bell, CheckCheck, ExternalLink, AlertTriangle, CheckCircle, Info } from 'lucide-react';

export const NotificationDrawer = () => {
  const {
    notifications,
    unreadCount,
    isDrawerOpen,
    closeDrawer,
    markAsRead,
    markAllAsRead,
  } = useNotifications();

  const navigate = useNavigate();

  if (!isDrawerOpen) return null;

  const handleNotificationClick = (item) => {
    if (!item.isRead) {
      markAsRead(item.id);
    }
    if (item.issueId) {
      closeDrawer();
      navigate(`/issues/${item.issueId}`);
    }
  };

  const getIcon = (type) => {
    switch (type) {
      case 'TASK_ASSIGNED':
      case 'URGENT_ACTION':
        return <AlertTriangle className="w-4 h-4 text-amber-500 shrink-0" />;
      case 'STATUS_CHANGE':
        return <CheckCircle className="w-4 h-4 text-emerald-500 shrink-0" />;
      default:
        return <Info className="w-4 h-4 text-blue-500 shrink-0" />;
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-hidden">
      {/* Backdrop */}
      <div
        className="fixed inset-0 bg-navy-950/50 backdrop-blur-xs transition-opacity"
        onClick={closeDrawer}
      />

      <div className="fixed inset-y-0 right-0 max-w-full flex pl-10">
        <div className="w-screen max-w-md bg-white shadow-2xl flex flex-col border-l border-slate-200">
          {/* Header */}
          <div className="p-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/60">
            <div className="flex items-center gap-2">
              <div className="p-2 bg-brand-50 text-brand-600 rounded-lg">
                <Bell className="w-5 h-5" />
              </div>
              <div>
                <h2 className="text-base font-bold text-slate-900">Notifications</h2>
                <p className="text-xs text-slate-500">
                  {unreadCount > 0 ? `${unreadCount} unread alert${unreadCount > 1 ? 's' : ''}` : 'All caught up'}
                </p>
              </div>
            </div>

            <div className="flex items-center gap-1">
              {unreadCount > 0 && (
                <button
                  onClick={markAllAsRead}
                  className="p-1.5 text-xs text-brand-600 hover:text-brand-700 hover:bg-brand-50 rounded-md font-medium flex items-center gap-1"
                  title="Mark all as read"
                >
                  <CheckCheck className="w-4 h-4" />
                  <span className="hidden sm:inline">Read all</span>
                </button>
              )}
              <button
                onClick={closeDrawer}
                className="p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-lg transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>
          </div>

          {/* List */}
          <div className="flex-1 overflow-y-auto divide-y divide-slate-100 p-2">
            {notifications.length === 0 ? (
              <div className="flex flex-col items-center justify-center h-64 text-center p-6 text-slate-400">
                <Bell className="w-12 h-12 text-slate-200 mb-3 stroke-[1.5]" />
                <p className="text-sm font-semibold text-slate-600">No notifications yet</p>
                <p className="text-xs mt-1">Updates on reported issues and task dispatches will appear here.</p>
              </div>
            ) : (
              notifications.map((item) => (
                <div
                  key={item.id}
                  onClick={() => handleNotificationClick(item)}
                  className={`p-3.5 rounded-xl transition-all cursor-pointer flex gap-3 ${
                    !item.isRead
                      ? 'bg-brand-50/40 hover:bg-brand-50/70 border-l-4 border-brand-500'
                      : 'hover:bg-slate-50 opacity-85'
                  }`}
                >
                  <div className="mt-0.5">{getIcon(item.notificationType)}</div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between gap-1 mb-0.5">
                      <p className={`text-xs font-semibold truncate ${!item.isRead ? 'text-slate-900' : 'text-slate-700'}`}>
                        {item.title}
                      </p>
                      {!item.isRead && (
                        <span className="w-2 h-2 rounded-full bg-brand-500 shrink-0" />
                      )}
                    </div>
                    <p className="text-xs text-slate-600 line-clamp-2 leading-relaxed">
                      {item.message}
                    </p>
                    <div className="flex items-center justify-between mt-2 pt-1 text-[11px] text-slate-400">
                      <span>{item.createdAt ? new Date(item.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : 'Recent'}</span>
                      {item.issueId && (
                        <span className="text-brand-600 font-medium flex items-center gap-0.5">
                          View details <ExternalLink className="w-3 h-3" />
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
