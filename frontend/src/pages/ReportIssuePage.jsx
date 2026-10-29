import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { categoryApi, issueApi, storageApi } from '../services/api';
import { LocationPicker } from '../components/map/LocationPicker';
import {
  Camera,
  Upload,
  AlertCircle,
  MapPin,
  Clock,
  ShieldCheck,
  Sparkles,
  X,
  CheckCircle2,
} from 'lucide-react';

export const ReportIssuePage = () => {
  const { user, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const [categories, setCategories] = useState([]);
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    categoryId: '',
    severity: 'MEDIUM',
    latitude: 30.7705,
    longitude: 76.575,
    addressText: '',
    imageUrls: [],
  });

  const [uploading, setUploading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    categoryApi
      .getAllCategories()
      .then((data) => {
        setCategories(data || []);
        if (data && data.length > 0 && !formData.categoryId) {
          setFormData((prev) => ({ ...prev, categoryId: data[0].id }));
        }
      })
      .catch((err) => console.error('Failed to load categories:', err));
  }, []);

  const selectedCategory = categories.find((c) => String(c.id) === String(formData.categoryId));

  const handleFileUpload = async (e) => {
    const files = Array.from(e.target.files || []);
    if (files.length === 0) return;
    if (formData.imageUrls.length + files.length > 5) {
      alert('You can attach a maximum of 5 photos per report');
      return;
    }

    setUploading(true);
    try {
      const uploaded = [];
      for (const file of files) {
        const url = await storageApi.uploadImage(file, 'issues');
        uploaded.push(url);
      }
      setFormData((prev) => ({
        ...prev,
        imageUrls: [...prev.imageUrls, ...uploaded],
      }));
    } catch (err) {
      console.warn('Storage upload error, using local preview fallback:', err);
      const mockUrls = files.map((f) => URL.createObjectURL(f));
      setFormData((prev) => ({
        ...prev,
        imageUrls: [...prev.imageUrls, ...mockUrls],
      }));
    } finally {
      setUploading(false);
    }
  };

  const removePhoto = (index) => {
    setFormData((prev) => ({
      ...prev,
      imageUrls: prev.imageUrls.filter((_, i) => i !== index),
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!isAuthenticated) {
      alert('Please log in or use the Quick Persona switcher at the top to submit a report.');
      return;
    }

    setError(null);
    setSubmitting(true);

    try {
      const payload = {
        title: formData.title.trim(),
        description: formData.description.trim(),
        categoryId: Number(formData.categoryId),
        severity: formData.severity,
        latitude: parseFloat(formData.latitude),
        longitude: parseFloat(formData.longitude),
        addressText: formData.addressText.trim(),
        imageUrls: formData.imageUrls,
      };

      const created = await issueApi.createIssue(payload);
      alert(`Issue report submitted successfully! Tracking Number: ${created.trackingNumber}`);
      navigate(`/issues/${created.id}`);
    } catch (err) {
      setError(err.message || 'Failed to submit report');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      {/* Page Header */}
      <div>
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-brand-50 text-brand-700 border border-brand-200 mb-2">
          <Sparkles className="w-3.5 h-3.5 text-brand-600" />
          <span>Verified Citizen Reporting</span>
        </div>
        <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">
          Report Broken Infrastructure or Hazard
        </h1>
        <p className="text-sm text-slate-500 mt-1">
          Provide accurate details, drop a map pin, and attach photos to route your report to the correct campus department.
        </p>
      </div>

      {error && (
        <div className="p-4 bg-rose-50 border border-rose-200 rounded-2xl flex items-start gap-3 text-rose-800 text-xs">
          <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
          <span>{error}</span>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Step 1: Photos & Evidence */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200/90 shadow-xs space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-bold text-slate-900 flex items-center gap-2">
              <Camera className="w-4 h-4 text-brand-600" />
              1. Photographic Evidence (Up to 5 photos)
            </h2>
            <span className="text-xs text-slate-400">
              {formData.imageUrls.length}/5 attached
            </span>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
            {formData.imageUrls.map((url, index) => (
              <div key={index} className="relative h-28 rounded-xl overflow-hidden border border-slate-200 bg-slate-50 group">
                <img src={url} alt={`Upload ${index + 1}`} className="w-full h-full object-cover" />
                <button
                  type="button"
                  onClick={() => removePhoto(index)}
                  className="absolute top-1.5 right-1.5 p-1 bg-navy-950/70 text-white rounded-md hover:bg-rose-600 transition-colors"
                >
                  <X className="w-3.5 h-3.5" />
                </button>
              </div>
            ))}

            {formData.imageUrls.length < 5 && (
              <label className="h-28 rounded-xl border-2 border-dashed border-slate-300 hover:border-brand-500 bg-slate-50/50 hover:bg-brand-50/30 flex flex-col items-center justify-center p-3 cursor-pointer transition-colors text-center">
                <Upload className="w-5 h-5 text-slate-400 mb-1" />
                <span className="text-[11px] font-semibold text-slate-600">
                  {uploading ? 'Uploading...' : 'Add Photo'}
                </span>
                <span className="text-[9px] text-slate-400">JPG, PNG (max 10MB)</span>
                <input
                  type="file"
                  accept="image/*"
                  multiple
                  onChange={handleFileUpload}
                  className="hidden"
                  disabled={uploading}
                />
              </label>
            )}
          </div>
        </div>

        {/* Step 2: Details & Classification */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200/90 shadow-xs space-y-4">
          <h2 className="text-sm font-bold text-slate-900 flex items-center gap-2">
            <ShieldCheck className="w-4 h-4 text-brand-600" />
            2. Issue Classification & Severity
          </h2>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Category
              </label>
              <select
                value={formData.categoryId}
                onChange={(e) => setFormData({ ...formData, categoryId: e.target.value })}
                className="w-full text-xs p-3 rounded-xl border border-slate-300 bg-white focus:ring-2 focus:ring-brand-500 outline-none"
                required
              >
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Perceived Severity
              </label>
              <select
                value={formData.severity}
                onChange={(e) => setFormData({ ...formData, severity: e.target.value })}
                className="w-full text-xs p-3 rounded-xl border border-slate-300 bg-white focus:ring-2 focus:ring-brand-500 outline-none"
                required
              >
                <option value="LOW">Low (Cosmetic, minor inconvenience)</option>
                <option value="MEDIUM">Medium (Needs repair within normal hours)</option>
                <option value="HIGH">High (Impacts classroom / hostel functioning)</option>
                <option value="CRITICAL">Critical (Immediate safety, electrical or water hazard)</option>
              </select>
            </div>
          </div>

          {selectedCategory && (
            <div className="p-3 bg-brand-50/70 rounded-xl border border-brand-200/80 flex items-center justify-between text-xs text-brand-900">
              <span className="flex items-center gap-1.5 font-medium">
                <Clock className="w-3.5 h-3.5 text-brand-600" />
                Target Resolution SLA: <strong>{selectedCategory.slaHours} hours</strong>
              </span>
              <span className="text-[11px] text-brand-700">
                Routed to: {selectedCategory.departmentName || 'Responsible Department'}
              </span>
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Issue Title
            </label>
            <input
              type="text"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              placeholder="e.g. Broken water tap causing flooding in Block C 2nd floor washroom"
              minLength={5}
              maxLength={200}
              required
              className="w-full text-xs sm:text-sm p-3 rounded-xl border border-slate-300 focus:ring-2 focus:ring-brand-500 focus:border-brand-500 outline-none"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Comprehensive Description
            </label>
            <textarea
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              placeholder="Describe what is broken, potential hazards, and any specific landmarks that help technicians locate the problem quickly..."
              minLength={10}
              maxLength={5000}
              rows={4}
              required
              className="w-full text-xs sm:text-sm p-3 rounded-xl border border-slate-300 focus:ring-2 focus:ring-brand-500 focus:border-brand-500 outline-none"
            />
          </div>
        </div>

        {/* Step 3: Location Pinpoint */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200/90 shadow-xs space-y-4">
          <h2 className="text-sm font-bold text-slate-900 flex items-center gap-2">
            <MapPin className="w-4 h-4 text-brand-600" />
            3. Pinpoint Physical Location
          </h2>

          <LocationPicker
            initialLat={formData.latitude}
            initialLng={formData.longitude}
            onLocationChange={(lat, lng) =>
              setFormData((prev) => ({ ...prev, latitude: lat, longitude: lng }))
            }
          />

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Location Description / Landmark
            </label>
            <input
              type="text"
              value={formData.addressText}
              onChange={(e) => setFormData({ ...formData, addressText: e.target.value })}
              placeholder="e.g. Academic Block B, Ground floor south staircase exit"
              className="w-full text-xs p-3 rounded-xl border border-slate-300 focus:ring-2 focus:ring-brand-500 outline-none"
            />
          </div>
        </div>

        {/* Submit Bar */}
        <div className="flex items-center justify-between pt-4 border-t border-slate-200">
          <p className="text-xs text-slate-500 hidden sm:block">
            Reports are immediately audited, timestamped, and assigned an immutable tracking number.
          </p>

          <button
            type="submit"
            disabled={submitting || uploading}
            className="w-full sm:w-auto px-8 py-3.5 rounded-xl font-bold text-white bg-brand-600 hover:bg-brand-700 shadow-md shadow-brand-600/25 active:scale-95 transition-all text-sm disabled:opacity-50"
          >
            {submitting ? 'Submitting Report...' : 'Publish Issue Report'}
          </button>
        </div>
      </form>
    </div>
  );
};
