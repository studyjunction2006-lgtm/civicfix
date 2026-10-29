import React, { useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import { Link } from 'react-router-dom';
import { StatusBadge } from '../common/StatusBadge';
import { PriorityBadge } from '../common/PriorityBadge';
import { MapPin, ExternalLink, Calendar, Users } from 'lucide-react';

// Fix leaflet icon default asset paths
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

// Custom colored HTML marker icons
const createMarkerIcon = (priority, status) => {
  let bgColor = '#059669'; // Emerald default
  if (priority === 'CRITICAL') bgColor = '#e11d48'; // Rose
  else if (priority === 'HIGH') bgColor = '#d97706'; // Amber
  else if (priority === 'MEDIUM') bgColor = '#2563eb'; // Blue
  else if (priority === 'LOW') bgColor = '#64748b'; // Slate

  if (status === 'RESOLVED' || status === 'CLOSED') {
    bgColor = '#10b981'; // Green
  }

  const html = `
    <div class="custom-map-marker" style="background-color: ${bgColor};">
      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
        <path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z"/>
        <circle cx="12" cy="10" r="3"/>
      </svg>
    </div>
  `;

  return L.divIcon({
    html,
    className: 'custom-leaflet-marker',
    iconSize: [36, 36],
    iconAnchor: [18, 36],
    popupAnchor: [0, -36],
  });
};

// Component to dynamically fit bounds to all visible issues
const ChangeView = ({ issues }) => {
  const map = useMap();

  useEffect(() => {
    if (issues && issues.length > 0) {
      const validPoints = issues
        .filter((i) => i.latitude && i.longitude)
        .map((i) => [parseFloat(i.latitude), parseFloat(i.longitude)]);

      if (validPoints.length > 0) {
        const bounds = L.latLngBounds(validPoints);
        map.fitBounds(bounds, { padding: [50, 50], maxZoom: 16 });
      }
    }
  }, [issues, map]);

  return null;
};

export const IssueMap = ({
  issues = [],
  center = [30.7705, 76.575],
  zoom = 15,
  height = '500px',
  onMarkerClick,
}) => {
  return (
    <div className="relative w-full rounded-2xl overflow-hidden shadow-md border border-slate-200/80" style={{ height }}>
      <MapContainer
        center={center}
        zoom={zoom}
        scrollWheelZoom={false}
        className="w-full h-full z-10"
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        <ChangeView issues={issues} />

        {issues
          .filter((item) => item.latitude && item.longitude)
          .map((issue) => {
            const lat = parseFloat(issue.latitude);
            const lng = parseFloat(issue.longitude);
            const markerIcon = createMarkerIcon(issue.calculatedPriority || issue.severity, issue.status);

            return (
              <Marker
                key={issue.id}
                position={[lat, lng]}
                icon={markerIcon}
                eventHandlers={{
                  click: () => onMarkerClick && onMarkerClick(issue),
                }}
              >
                <Popup className="civicfix-popup">
                  <div className="w-72 p-3 font-sans">
                    {/* Image preview if present */}
                    {issue.images && issue.images.length > 0 && (
                      <div className="h-28 w-full -mx-3 -mt-3 mb-2.5 overflow-hidden rounded-t-xl bg-slate-100">
                        <img
                          src={issue.images[0].imageUrl}
                          alt={issue.title}
                          className="w-full h-full object-cover"
                        />
                      </div>
                    )}

                    <div className="flex items-center justify-between gap-1 mb-1.5">
                      <span className="text-[10px] font-mono font-bold text-slate-500 bg-slate-100 px-1.5 py-0.5 rounded">
                        {issue.trackingNumber}
                      </span>
                      <PriorityBadge priority={issue.calculatedPriority || issue.severity} />
                    </div>

                    <h4 className="text-sm font-bold text-slate-900 line-clamp-1 mb-1">
                      {issue.title}
                    </h4>

                    <p className="text-xs text-slate-600 line-clamp-2 mb-2 leading-relaxed">
                      {issue.description}
                    </p>

                    <div className="flex items-center gap-1.5 text-[11px] text-slate-500 mb-2">
                      <MapPin className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                      <span className="truncate">{issue.addressText || `${lat.toFixed(4)}, ${lng.toFixed(4)}`}</span>
                    </div>

                    <div className="flex items-center justify-between pt-2 border-t border-slate-100">
                      <StatusBadge status={issue.status} />

                      <div className="flex items-center gap-2">
                        {issue.confirmationCount > 0 && (
                          <span className="text-[11px] text-brand-700 font-semibold flex items-center gap-0.5" title="Community confirmations">
                            <Users className="w-3 h-3" />
                            {issue.confirmationCount}
                          </span>
                        )}
                        <Link
                          to={`/issues/${issue.id}`}
                          className="text-xs font-semibold text-brand-600 hover:text-brand-700 flex items-center gap-0.5"
                        >
                          View <ExternalLink className="w-3 h-3" />
                        </Link>
                      </div>
                    </div>
                  </div>
                </Popup>
              </Marker>
            );
          })}
      </MapContainer>
    </div>
  );
};
