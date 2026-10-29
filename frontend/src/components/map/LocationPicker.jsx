import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, useMapEvents } from 'react-leaflet';
import L from 'leaflet';
import { Crosshair, MapPin, Check } from 'lucide-react';

const pinIcon = L.divIcon({
  html: `
    <div style="background-color: #059669; width: 34px; height: 34px; border-radius: 50%; display: flex; align-items: center; justify-content: center; border: 3px solid white; box-shadow: 0 4px 10px rgba(0,0,0,0.3);">
      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
        <path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z"/>
        <circle cx="12" cy="10" r="3"/>
      </svg>
    </div>
  `,
  className: 'location-picker-pin',
  iconSize: [34, 34],
  iconAnchor: [17, 34],
});

const MapClickHandler = ({ onLocationSelected }) => {
  useMapEvents({
    click(e) {
      onLocationSelected(e.latlng.lat, e.latlng.lng);
    },
  });
  return null;
};

export const LocationPicker = ({
  initialLat = 30.7705,
  initialLng = 76.575,
  onLocationChange,
}) => {
  const [position, setPosition] = useState([initialLat, initialLng]);
  const [gpsLoading, setGpsLoading] = useState(false);

  useEffect(() => {
    if (initialLat && initialLng) {
      setPosition([initialLat, initialLng]);
    }
  }, [initialLat, initialLng]);

  const handleSelect = (lat, lng) => {
    setPosition([lat, lng]);
    if (onLocationChange) {
      onLocationChange(lat, lng);
    }
  };

  const handleGetCurrentLocation = () => {
    if (!navigator.geolocation) {
      alert('Geolocation is not supported by your browser');
      return;
    }
    setGpsLoading(true);
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const { latitude, longitude } = pos.coords;
        handleSelect(latitude, longitude);
        setGpsLoading(false);
      },
      (err) => {
        console.warn('Geolocation error:', err.message);
        // Default to campus location if GPS denied
        alert('Could not retrieve current location. Please click on the map to set location.');
        setGpsLoading(false);
      },
      { timeout: 10000, enableHighAccuracy: true }
    );
  };

  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between text-xs text-slate-600 mb-1">
        <span className="flex items-center gap-1 font-medium">
          <MapPin className="w-3.5 h-3.5 text-brand-600" />
          Click on map to drop location pin
        </span>
        <button
          type="button"
          onClick={handleGetCurrentLocation}
          disabled={gpsLoading}
          className="inline-flex items-center gap-1.5 px-2.5 py-1 text-xs font-semibold text-brand-700 bg-brand-50 hover:bg-brand-100 rounded-lg transition-colors border border-brand-200"
        >
          <Crosshair className={`w-3.5 h-3.5 ${gpsLoading ? 'animate-spin' : ''}`} />
          {gpsLoading ? 'Detecting...' : 'Use My GPS'}
        </button>
      </div>

      <div className="h-64 w-full rounded-xl overflow-hidden border border-slate-300 relative shadow-inner">
        <MapContainer
          center={position}
          zoom={16}
          scrollWheelZoom={false}
          className="h-full w-full"
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          <MapClickHandler onLocationSelected={handleSelect} />
          <Marker
            position={position}
            icon={pinIcon}
            draggable={true}
            eventHandlers={{
              dragend: (e) => {
                const marker = e.target;
                const latLng = marker.getLatLng();
                handleSelect(latLng.lat, latLng.lng);
              },
            }}
          />
        </MapContainer>
      </div>

      <div className="flex items-center justify-between text-[11px] text-slate-500 bg-slate-50 px-3 py-1.5 rounded-lg border border-slate-200">
        <span>Selected Coordinates:</span>
        <span className="font-mono font-medium text-slate-700">
          {position[0]?.toFixed(6)}, {position[1]?.toFixed(6)}
        </span>
      </div>
    </div>
  );
};
