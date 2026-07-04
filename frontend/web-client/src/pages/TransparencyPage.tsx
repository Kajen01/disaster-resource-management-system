import { useState } from "react";
import SectionCard from "../components/SectionCard";
import api from "../lib/api";
import { getApiErrorMessage } from "../lib/apiError";
import { useAuth } from "../app/AuthContext";
import type { DonationTraceSummary, TransparencyView } from "../lib/types";

export default function TransparencyPage() {
  const { auth } = useAuth();
  const [donationRef, setDonationRef] = useState("");
  const [transparency, setTransparency] = useState<TransparencyView | null>(null);
  const [traces, setTraces] = useState<DonationTraceSummary[]>([]);
  const [error, setError] = useState("");

  async function lookup() {
    setError("");
    try {
      const { data } = await api.get<TransparencyView>(`/api/transparency/donations/${donationRef}`);
      setTransparency(data);
    } catch (err) {
      setError(getApiErrorMessage(err, "Could not trace the donation reference."));
    }
  }

  async function loadTraces() {
    setError("");
    try {
      const { data } = await api.get<DonationTraceSummary[]>("/api/transparency/traces");
      setTraces(data);
    } catch (err) {
      setError(getApiErrorMessage(err, "Could not load the global transparency logs."));
    }
  }

  return (
    <div className="page-grid single">
      <SectionCard title="Donation Transparency">
        <div className="inline-grid">
          <label className="field">
            <span className="field-label">Donation reference</span>
            <input value={donationRef} onChange={(e) => setDonationRef(e.target.value)} placeholder="DON-2026-001" />
          </label>
          <button className="primary-button" onClick={() => void lookup()}>Trace donation</button>
        </div>
        {error ? <p className="error-text">{error}</p> : null}
        {transparency ? (
          <div className="stack">
            <p><strong>Resource:</strong> {transparency.resourceName} ({transparency.resourceType})</p>
            <p><strong>Path:</strong> Shelter {transparency.sourceShelterId} to Shelter {transparency.destinationShelterId}</p>
            <p><strong>Quantity:</strong> {transparency.quantity}</p>
            <div className="timeline">
              {transparency.timeline.map((event) => (
                <div className="timeline-item" key={`${event.eventType}-${event.occurredAt}`}>
                  <strong>{event.eventType}</strong>
                  <p>{event.details}</p>
                  <span>{new Date(event.occurredAt).toLocaleString()}</span>
                </div>
              ))}
            </div>
          </div>
        ) : null}
      </SectionCard>
      {auth?.role === "ADMIN" ? (
        <SectionCard title="Global Transparency Logs">
          <button className="secondary-button" onClick={() => void loadTraces()}>Load global traces</button>
          <div className="table-like">
            {traces.map((trace) => (
              <div className="table-row" key={`${trace.transferId}-${trace.donationRef}`}>
                <div>
                  <strong>{trace.donationRef}</strong>
                  <p className="muted">{trace.resourceName}</p>
                </div>
                <div>
                  <p>{trace.sourceShelterId} to {trace.destinationShelterId}</p>
                  <p className="muted">{new Date(trace.recordedAt).toLocaleString()}</p>
                </div>
              </div>
            ))}
          </div>
        </SectionCard>
      ) : null}
    </div>
  );
}
