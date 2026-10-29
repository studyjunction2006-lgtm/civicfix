import React from 'react';
import { useAuth, DEMO_ACCOUNTS } from '../../context/AuthContext';
import { Sparkles, ShieldCheck, UserCheck, Wrench, GraduationCap } from 'lucide-react';

const ROLE_ICONS = {
  CITIZEN: GraduationCap,
  AUTHORITY: ShieldCheck,
  WORKER: Wrench,
  ADMIN: UserCheck,
};

export const DemoBar = () => {
  const { user, switchDemoUser } = useAuth();

  return (
    <div className="bg-navy-950 text-slate-300 border-b border-navy-800 text-xs py-1.5 px-4 sticky top-0 z-40 transition-all">
      <div className="max-w-7xl mx-auto flex flex-wrap items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <span className="flex items-center gap-1.5 font-semibold text-emerald-400 bg-emerald-950/80 border border-emerald-800/60 px-2 py-0.5 rounded-full">
            <Sparkles className="w-3.5 h-3.5" />
            Quick Demo Switcher
          </span>
          <span className="hidden md:inline text-slate-400">
            Switch persona to test full end-to-end workflow:
          </span>
        </div>

        <div className="flex items-center gap-1.5 flex-wrap">
          {DEMO_ACCOUNTS.map((acc) => {
            const Icon = ROLE_ICONS[acc.role] || UserCheck;
            const isActive = user?.email === acc.email;

            return (
              <button
                key={acc.role}
                onClick={() => switchDemoUser(acc.role)}
                className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md font-medium transition-all ${
                  isActive
                    ? 'bg-brand-600 text-white shadow-sm ring-1 ring-brand-400'
                    : 'bg-navy-900/90 hover:bg-navy-800 text-slate-300 hover:text-white border border-navy-700/60'
                }`}
                title={`Switch to ${acc.name} (${acc.badge})`}
              >
                <Icon className="w-3 h-3 text-slate-300" />
                <span>{acc.label}</span>
                {isActive && <span className="w-1.5 h-1.5 rounded-full bg-white ml-0.5" />}
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
};
