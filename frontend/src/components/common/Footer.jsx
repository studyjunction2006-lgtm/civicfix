import React from 'react';
import { ShieldCheck, Heart, Github } from 'lucide-react';
import { Link } from 'react-router-dom';

export const Footer = () => {
  return (
    <footer className="bg-navy-950 text-slate-400 border-t border-navy-800 text-sm mt-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          {/* Brand Info */}
          <div className="md:col-span-2 space-y-3">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-lg bg-brand-600 flex items-center justify-center text-white">
                <ShieldCheck className="w-5 h-5" />
              </div>
              <span className="font-extrabold text-lg text-white tracking-tight">
                Civic<span className="text-brand-500">Fix</span>
              </span>
            </div>
            <p className="text-xs text-slate-400 max-w-sm leading-relaxed">
              Transparent, accountable community problem reporting and resolution management system. Empowering campus and civic communities to report issues, prevent duplicates, track dispatch workflows, and verify quality fixes.
            </p>
            <div className="pt-2 flex items-center gap-2 text-xs text-slate-500">
              <span>Demo Platform</span>
              <span>•</span>
              <span>Spring Boot 3 + React + Leaflet</span>
            </div>
          </div>

          {/* Quick Links */}
          <div>
            <h4 className="text-xs font-bold text-white uppercase tracking-wider mb-3">
              Platform
            </h4>
            <ul className="space-y-2 text-xs">
              <li>
                <Link to="/explore" className="hover:text-white transition-colors">
                  Live Issue Map
                </Link>
              </li>
              <li>
                <Link to="/report" className="hover:text-white transition-colors">
                  Report Broken Infrastructure
                </Link>
              </li>
              <li>
                <Link to="/track" className="hover:text-white transition-colors">
                  Tracking Number Lookup
                </Link>
              </li>
            </ul>
          </div>

          {/* Roles & Portals */}
          <div>
            <h4 className="text-xs font-bold text-white uppercase tracking-wider mb-3">
              Role Workflows
            </h4>
            <ul className="space-y-2 text-xs">
              <li>
                <span className="text-emerald-400 font-medium">Citizen:</span> Report, upvote, verify
              </li>
              <li>
                <span className="text-indigo-400 font-medium">Authority:</span> Review, verify, dispatch
              </li>
              <li>
                <span className="text-amber-400 font-medium">Worker:</span> Accept, resolve, evidence
              </li>
              <li>
                <span className="text-rose-400 font-medium">Admin:</span> Audit logs, SLA, user roles
              </li>
            </ul>
          </div>
        </div>

        <div className="mt-8 pt-6 border-t border-navy-800/80 flex flex-col sm:flex-row items-center justify-between text-xs text-slate-500 gap-4">
          <p>© 2026 CivicFix Platform. Built with transparency in mind.</p>
          <p className="flex items-center gap-1">
            Engineered for civic impact with <Heart className="w-3.5 h-3.5 text-rose-500 fill-rose-500 inline" />
          </p>
        </div>
      </div>
    </footer>
  );
};
