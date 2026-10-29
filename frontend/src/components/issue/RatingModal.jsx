import React, { useState } from 'react';
import { Modal } from '../common/Modal';
import { ratingApi } from '../../services/api';
import { Star } from 'lucide-react';

export const RatingModal = ({ isOpen, onClose, issueId, onRatingSubmitted }) => {
  const [rating, setRating] = useState(5);
  const [hoverRating, setHoverRating] = useState(0);
  const [feedback, setFeedback] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await ratingApi.submitRating(issueId, { rating, feedback });
      alert('Thank you! Your feedback has been submitted.');
      if (onRatingSubmitted) onRatingSubmitted(res);
      onClose();
    } catch (err) {
      alert(err.message || 'Failed to submit satisfaction rating');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Rate Resolution Quality">
      <form onSubmit={handleSubmit} className="space-y-4">
        <p className="text-xs text-slate-600 leading-relaxed">
          How satisfied are you with the speed, cleanliness, and completeness of this repair?
        </p>

        {/* Stars */}
        <div className="flex items-center justify-center gap-2 py-2">
          {[1, 2, 3, 4, 5].map((star) => {
            const isFilled = star <= (hoverRating || rating);
            return (
              <button
                type="button"
                key={star}
                onMouseEnter={() => setHoverRating(star)}
                onMouseLeave={() => setHoverRating(0)}
                onClick={() => setRating(star)}
                className="p-1 focus:outline-none transition-transform hover:scale-125"
              >
                <Star
                  className={`w-8 h-8 ${
                    isFilled
                      ? 'text-amber-400 fill-amber-400'
                      : 'text-slate-300'
                  }`}
                />
              </button>
            );
          })}
        </div>

        <div className="text-center text-xs font-semibold text-slate-700">
          {rating === 5 && '🌟 Excellent — Exceeded expectations!'}
          {rating === 4 && '👍 Good — Thorough and completed properly'}
          {rating === 3 && '😐 Satisfactory — Problem resolved with minor delay'}
          {rating === 2 && '👎 Poor — Subpar workmanship or mess left'}
          {rating === 1 && '⚠️ Very Unsatisfied — Defect remains incomplete'}
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-700 mb-1">
            Feedback & Comments (Optional)
          </label>
          <textarea
            value={feedback}
            onChange={(e) => setFeedback(e.target.value)}
            placeholder="Share details on how the technician performed..."
            rows={3}
            className="w-full text-xs p-3 rounded-xl border border-slate-300 focus:ring-2 focus:ring-brand-500 focus:border-brand-500 outline-none"
          />
        </div>

        <div className="flex items-center justify-end gap-2 pt-2 border-t border-slate-100">
          <button
            type="button"
            onClick={onClose}
            className="px-3.5 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-lg transition-colors"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={loading}
            className="px-4 py-2 text-xs font-semibold text-white bg-brand-600 hover:bg-brand-700 rounded-xl shadow-xs transition-colors disabled:opacity-50"
          >
            {loading ? 'Submitting...' : 'Submit Rating'}
          </button>
        </div>
      </form>
    </Modal>
  );
};
