import React from 'react';
import { Link } from 'react-router-dom';
import { StatusBadge } from '../common/StatusBadge';
import { PriorityBadge } from '../common/PriorityBadge';
import { MapPin, Users, Calendar, ArrowRight, Image as ImageIcon } from 'lucide-react';

export const IssueCard = ({ issue }) => {
  const thumbnail = issue.images && issue.images.length > 0 ? issue.images[0].imageUrl : null;

  return (
    <div className="group bg-white rounded-2xl border border-slate-200/90 hover:border-brand-500/50 hover:shadow-xl transition-all duration-300 flex flex-col overflow-hidden">
      {/* Optional Photo Header */}
      {thumbnail && (
        <div className="relative h-44 w-full overflow-hidden bg-slate-100">
          <img
            src={thumbnail}
            alt={issue.title}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
          />
          <div className="absolute top-3 left-3">
            <span className="font-mono text-xs font-bold bg-navy-950/80 text-white backdrop-blur-md px-2 py-1 rounded-md shadow-xs">
              {issue.trackingNumber}
            </span>
          </div>
          {issue.images.length > 1 && (
            <div className="absolute bottom-3 right-3 bg-navy-950/70 text-white text-[11px] font-semibold px-2 py-0.5 rounded-full flex items-center gap-1 backdrop-blur-xs">
              <ImageIcon className="w-3 h-3" />
              +{issue.images.length - 1} photos
            </div>
          )}
        </div>
      )}

      <div className="p-5 flex-1 flex flex-col">
        {!thumbnail && (
          <div className="flex items-center justify-between mb-3">
            <span className="font-mono text-xs font-bold text-slate-500 bg-slate-100 px-2 py-0.5 rounded-md">
              {issue.trackingNumber}
            </span>
            <PriorityBadge priority={issue.calculatedPriority || issue.severity} score={issue.priorityScore} />
          </div>
        )}

        {thumbnail && (
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-semibold text-brand-600 truncate max-w-[180px]">
              {issue.categoryName || 'General Category'}
            </span>
            <PriorityBadge priority={issue.calculatedPriority || issue.severity} score={issue.priorityScore} />
          </div>
        )}

        <h3 className="text-base font-bold text-slate-900 group-hover:text-brand-600 transition-colors line-clamp-2 mb-2 leading-snug">
          <Link to={`/issues/${issue.id}`} className="hover:underline">
            {issue.title}
          </Link>
        </h3>

        <p className="text-xs text-slate-500 line-clamp-2 leading-relaxed mb-4 flex-1">
          {issue.description}
        </p>

        {/* Location Info */}
        <div className="flex items-center gap-1.5 text-xs text-slate-500 mb-4 bg-slate-50 px-2.5 py-1.5 rounded-lg border border-slate-100">
          <MapPin className="w-3.5 h-3.5 text-slate-400 shrink-0" />
          <span className="truncate">{issue.addressText || 'Campus Grounds'}</span>
        </div>

        {/* Footer Meta */}
        <div className="pt-3 border-t border-slate-100 flex items-center justify-between gap-2">
          <StatusBadge status={issue.status} />

          <div className="flex items-center gap-3">
            {issue.confirmationCount > 0 && (
              <span
                className="inline-flex items-center gap-1 text-xs font-semibold text-slate-600 bg-slate-100 hover:bg-slate-200 px-2 py-0.5 rounded-full transition-colors"
                title={`${issue.confirmationCount} citizens confirmed this problem`}
              >
                <Users className="w-3.5 h-3.5 text-brand-600" />
                {issue.confirmationCount}
              </span>
            )}

            <Link
              to={`/issues/${issue.id}`}
              className="inline-flex items-center justify-center p-1.5 text-slate-400 group-hover:text-brand-600 hover:bg-brand-50 rounded-lg transition-colors"
              aria-label="View issue details"
            >
              <ArrowRight className="w-4 h-4 group-hover:translate-x-0.5 transition-transform" />
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};
