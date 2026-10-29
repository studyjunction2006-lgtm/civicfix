import React from 'react';
import { StatusBadge } from '../common/StatusBadge';
import { Clock, User, MessageSquare } from 'lucide-react';

export const TimelineStepper = ({ history = [] }) => {
  if (!history || history.length === 0) {
    return (
      <div className="p-4 text-center text-xs text-slate-400 bg-slate-50 rounded-xl">
        No workflow status transitions recorded yet.
      </div>
    );
  }

  return (
    <div className="relative pl-6 space-y-6 before:absolute before:left-2.5 before:top-3 before:bottom-3 before:w-0.5 before:bg-slate-200">
      {history.map((item, index) => {
        const isLatest = index === 0 || index === history.length - 1;

        return (
          <div key={item.id || index} className="relative group">
            {/* Dot */}
            <div
              className={`absolute -left-6 top-1.5 w-5 h-5 rounded-full border-2 border-white flex items-center justify-center transition-all ${
                isLatest
                  ? 'bg-brand-600 ring-4 ring-brand-100 shadow-sm'
                  : 'bg-slate-300 group-hover:bg-slate-400'
              }`}
            >
              <div className="w-1.5 h-1.5 rounded-full bg-white" />
            </div>

            {/* Content card */}
            <div className="bg-white rounded-xl border border-slate-200/80 p-3.5 shadow-xs group-hover:border-slate-300 transition-colors">
              <div className="flex flex-wrap items-center justify-between gap-2 mb-1.5">
                <div className="flex items-center gap-2">
                  <StatusBadge status={item.newStatus} />
                  {item.previousStatus && (
                    <span className="text-[11px] text-slate-400 hidden sm:inline">
                      from {item.previousStatus.replace(/_/g, ' ')}
                    </span>
                  )}
                </div>

                <div className="flex items-center gap-1 text-[11px] text-slate-400 font-medium">
                  <Clock className="w-3 h-3" />
                  <span>
                    {item.createdAt
                      ? new Date(item.createdAt).toLocaleString([], {
                          month: 'short',
                          day: 'numeric',
                          hour: '2-digit',
                          minute: '2-digit',
                        })
                      : 'Recently'}
                  </span>
                </div>
              </div>

              {/* Actor */}
              <div className="flex items-center gap-1.5 text-xs text-slate-600 mb-1">
                <User className="w-3.5 h-3.5 text-slate-400" />
                <span className="font-semibold text-slate-900">
                  {item.actorName || item.actorEmail || 'System / Citizen'}
                </span>
                {item.actorRole && (
                  <span className="text-[10px] uppercase font-bold tracking-wider px-1.5 py-0.2 rounded bg-slate-100 text-slate-600">
                    {item.actorRole}
                  </span>
                )}
              </div>

              {/* Notes */}
              {item.notes && (
                <div className="mt-2 text-xs text-slate-600 bg-slate-50 p-2.5 rounded-lg border border-slate-100 flex items-start gap-1.5">
                  <MessageSquare className="w-3.5 h-3.5 text-slate-400 shrink-0 mt-0.5" />
                  <p className="leading-relaxed">{item.notes}</p>
                </div>
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
};
