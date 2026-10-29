import React, { useState, useEffect } from 'react';
import { issueApi, departmentApi, categoryApi } from '../services/api';
import { IssueCard } from '../components/issue/IssueCard';
import { IssueMap } from '../components/map/IssueMap';
import {
  Search,
  Filter,
  LayoutGrid,
  Map as MapIcon,
  ChevronLeft,
  ChevronRight,
  RotateCcw,
  SlidersHorizontal,
} from 'lucide-react';

export const ExplorePage = () => {
  const [issues, setIssues] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);

  // Filter States
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('');
  const [selectedPriority, setSelectedPriority] = useState('');
  const [selectedDepartment, setSelectedDepartment] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [sortBy, setSortBy] = useState('createdAt');
  const [sortDir, setSortDir] = useState('DESC');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // View state
  const [viewMode, setViewMode] = useState('grid'); // 'grid' or 'split'

  // Fetch departments & categories on mount
  useEffect(() => {
    departmentApi.getAllDepartments().then(setDepartments).catch(console.error);
    categoryApi.getAllCategories().then(setCategories).catch(console.error);
  }, []);

  // Fetch issues whenever filters change
  const fetchIssues = async () => {
    setLoading(true);
    try {
      const params = {
        page,
        size: viewMode === 'split' ? 8 : 9,
        sortBy,
        sortDir,
      };

      if (searchQuery.trim()) params.query = searchQuery.trim();
      if (selectedStatus) params.status = selectedStatus;
      if (selectedPriority) params.priority = selectedPriority;
      if (selectedDepartment) params.departmentId = selectedDepartment;
      if (selectedCategory) params.categoryId = selectedCategory;

      const res = await issueApi.searchIssues(params);
      const items = res.content || res || [];
      setIssues(items);
      setTotalPages(res.totalPages || 1);
      setTotalElements(res.totalElements || items.length);
    } catch (err) {
      console.error('Failed to search issues:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchIssues();
  }, [
    page,
    selectedStatus,
    selectedPriority,
    selectedDepartment,
    selectedCategory,
    sortBy,
    sortDir,
    viewMode,
  ]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setPage(0);
    fetchIssues();
  };

  const handleResetFilters = () => {
    setSearchQuery('');
    setSelectedStatus('');
    setSelectedPriority('');
    setSelectedDepartment('');
    setSelectedCategory('');
    setSortBy('createdAt');
    setSortDir('DESC');
    setPage(0);
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
      {/* Header & View Switcher */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            Explore Campus Issues
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Browse active reports, filter by department, or explore the live geo-spatial map.
          </p>
        </div>

        {/* View Toggle */}
        <div className="flex items-center gap-1 bg-slate-100 p-1 rounded-xl border border-slate-200">
          <button
            onClick={() => setViewMode('grid')}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              viewMode === 'grid'
                ? 'bg-white text-slate-900 shadow-xs'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            <LayoutGrid className="w-3.5 h-3.5" />
            Grid View
          </button>
          <button
            onClick={() => setViewMode('split')}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              viewMode === 'split'
                ? 'bg-white text-slate-900 shadow-xs'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            <MapIcon className="w-3.5 h-3.5" />
            Map + List
          </button>
        </div>
      </div>

      {/* Search & Filter Bar */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200/90 shadow-xs space-y-3">
        <form onSubmit={handleSearchSubmit} className="flex gap-2">
          <div className="relative flex-1">
            <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search by keywords, tracking number (e.g. CF-2026-0001), or title..."
              className="w-full text-xs sm:text-sm pl-10 pr-4 py-2.5 rounded-xl border border-slate-300 focus:ring-2 focus:ring-brand-500 focus:border-brand-500 outline-none"
            />
          </div>
          <button
            type="submit"
            className="px-5 py-2.5 rounded-xl text-xs sm:text-sm font-semibold text-white bg-brand-600 hover:bg-brand-700 shadow-xs transition-colors shrink-0"
          >
            Search
          </button>
        </form>

        {/* Filter Selectors */}
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-2 pt-1 text-xs">
          {/* Status */}
          <select
            value={selectedStatus}
            onChange={(e) => {
              setSelectedStatus(e.target.value);
              setPage(0);
            }}
            className="p-2 rounded-xl border border-slate-300 bg-white text-slate-700 outline-none focus:ring-2 focus:ring-brand-500"
          >
            <option value="">All Statuses</option>
            <option value="REPORT_CREATED">Report Created</option>
            <option value="UNDER_REVIEW">Under Review</option>
            <option value="VERIFIED">Verified</option>
            <option value="ASSIGNED">Assigned</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="RESOLVED">Resolved</option>
            <option value="CLOSED">Closed</option>
          </select>

          {/* Priority */}
          <select
            value={selectedPriority}
            onChange={(e) => {
              setSelectedPriority(e.target.value);
              setPage(0);
            }}
            className="p-2 rounded-xl border border-slate-300 bg-white text-slate-700 outline-none focus:ring-2 focus:ring-brand-500"
          >
            <option value="">All Priorities</option>
            <option value="CRITICAL">Critical</option>
            <option value="HIGH">High</option>
            <option value="MEDIUM">Medium</option>
            <option value="LOW">Low</option>
          </select>

          {/* Department */}
          <select
            value={selectedDepartment}
            onChange={(e) => {
              setSelectedDepartment(e.target.value);
              setPage(0);
            }}
            className="p-2 rounded-xl border border-slate-300 bg-white text-slate-700 outline-none focus:ring-2 focus:ring-brand-500 truncate"
          >
            <option value="">All Departments</option>
            {departments.map((d) => (
              <option key={d.id} value={d.id}>
                {d.name}
              </option>
            ))}
          </select>

          {/* Category */}
          <select
            value={selectedCategory}
            onChange={(e) => {
              setSelectedCategory(e.target.value);
              setPage(0);
            }}
            className="p-2 rounded-xl border border-slate-300 bg-white text-slate-700 outline-none focus:ring-2 focus:ring-brand-500 truncate"
          >
            <option value="">All Categories</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>

          {/* Sort By */}
          <select
            value={sortBy}
            onChange={(e) => {
              setSortBy(e.target.value);
              setPage(0);
            }}
            className="p-2 rounded-xl border border-slate-300 bg-white text-slate-700 outline-none focus:ring-2 focus:ring-brand-500"
          >
            <option value="createdAt">Date Reported</option>
            <option value="priorityScore">Priority Score</option>
            <option value="confirmationCount">Confirmations</option>
          </select>

          {/* Reset Filters */}
          <button
            type="button"
            onClick={handleResetFilters}
            className="p-2 rounded-xl border border-slate-200 text-slate-600 hover:bg-slate-50 flex items-center justify-center gap-1 font-medium transition-colors"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            Reset Filters
          </button>
        </div>
      </div>

      {/* Main Content Area */}
      {viewMode === 'grid' ? (
        // Grid View
        <div>
          {loading ? (
            <div className="py-20 text-center text-slate-400">Loading issues...</div>
          ) : issues.length === 0 ? (
            <div className="py-20 text-center bg-white rounded-2xl border border-slate-200">
              <p className="text-base font-semibold text-slate-700">No issues match your criteria</p>
              <p className="text-xs text-slate-400 mt-1">Try clearing filters or search query.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {issues.map((issue) => (
                <IssueCard key={issue.id} issue={issue} />
              ))}
            </div>
          )}
        </div>
      ) : (
        // Split Map + List View
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
          <div className="lg:col-span-7 sticky top-24">
            <IssueMap issues={issues} height="600px" />
          </div>

          <div className="lg:col-span-5 space-y-4 max-h-[600px] overflow-y-auto pr-1">
            {issues.map((issue) => (
              <IssueCard key={issue.id} issue={issue} />
            ))}
          </div>
        </div>
      )}

      {/* Pagination Bar */}
      {totalPages > 1 && (
        <div className="flex items-center justify-between pt-6 border-t border-slate-200 text-xs">
          <span className="text-slate-500">
            Showing Page <span className="font-semibold text-slate-900">{page + 1}</span> of{' '}
            <span className="font-semibold text-slate-900">{totalPages}</span> ({totalElements} total)
          </span>

          <div className="flex items-center gap-2">
            <button
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              disabled={page === 0}
              className="p-2 rounded-lg border border-slate-300 hover:bg-slate-50 disabled:opacity-40 transition-colors"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <button
              onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
              disabled={page >= totalPages - 1}
              className="p-2 rounded-lg border border-slate-300 hover:bg-slate-50 disabled:opacity-40 transition-colors"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
