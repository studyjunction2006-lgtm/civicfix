import React from 'react';
import { AlertCircle, Flame, ArrowUp, ArrowDown } from 'lucide-react';

const PRIORITY_CONFIG = {
  CRITICAL: {
    label: 'Critical',
    bg: 'bg-rose-100',
    text: 'text-rose-800',
    border: 'border-rose-300',
    icon: Flame,
  },
  HIGH: {
    label: 'High',
    bg: 'bg-amber-100',
    text: 'text-amber-800',
    border: 'border-amber-300',
    icon: AlertCircle,
  },
  MEDIUM: {
    label: 'Medium',
    bg: 'bg-blue-100',
    text: 'text-blue-800',
    border: 'border-blue-300',
    icon: ArrowUp,
  },
  LOW: {
    label: 'Low',
    bg: 'bg-slate-100',
    text: 'text-slate-700',
    border: 'border-slate-300',
    icon: ArrowDown,
  },
};

export const PriorityBadge = ({ priority, score }) => {
  const config = PRIORITY_CONFIG[priority] || PRIORITY_CONFIG.MEDIUM;
  const Icon = config.icon;

  return (
    <span
      className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold border ${config.bg} ${config.text} ${config.border}`}
      title={score ? `Priority Score: ${score}` : undefined}
    >
      <Icon className="w-3.5 h-3.5" />
      <span>{config.label}</span>
      {score !== undefined && score !== null && (
        <span className="opacity-75 text-[10px] ml-0.5">({Math.round(score)})</span>
      )}
    </span>
  );
};

export const SeverityBadge = ({ severity }) => {
  const config = PRIORITY_CONFIG[severity] || PRIORITY_CONFIG.MEDIUM;
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded text-[11px] font-medium border ${config.bg} ${config.text} ${config.border}`}>
      Severity: {severity}
    </span>
  );
};
