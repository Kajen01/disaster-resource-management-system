import { FormEvent, useEffect, useState } from "react";
import SectionCard from "../components/SectionCard";
import api from "../lib/api";
import { getApiErrorMessage } from "../lib/apiError";
import type { ResourceBatch, Shelter, UserResponse, TransparencyView } from "../lib/types";

interface LogDonationItem {
  resourceType: string;
  resourceName: string;
  unit: string;
  quantityReceived: number;
  expiryDate: string;
}

export default function LogDonationPage() {
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [activeDonors, setActiveDonors] = useState<UserResponse[]>([]);
  const [sheltersList, setSheltersList] = useState<Shelter[]>([]);
  const [batches, setBatches] = useState<ResourceBatch[]>([]);
  
  // Multi-item donation states
  const [donorEmail, setDonorEmail] = useState("");
  const [shelterId, setShelterId] = useState(0);
  const [items, setItems] = useState<LogDonationItem[]>([
    { resourceType: "FOOD", resourceName: "", unit: "units", quantityReceived: 1, expiryDate: "" }
  ]);
  const [createdRefs, setCreatedRefs] = useState<string[]>([]);

  // Track Modal State
  const [trackRef, setTrackRef] = useState<string | null>(null);
  const [timelineData, setTimelineData] = useState<TransparencyView | null>(null);
  const [trackError, setTrackError] = useState("");

  async function loadData() {
    try {
      const { data: users } = await api.get<UserResponse[]>("/api/users");
      const donors = users.filter((u) => u.role === "DONOR" && u.status === "ACTIVE");
      setActiveDonors(donors);
      if (donors.length > 0 && !donorEmail) {
        setDonorEmail(donors[0].email);
      }

      const { data: shelters } = await api.get<Shelter[]>("/api/shelters");
      setSheltersList(shelters.filter((s) => s.status === "ACTIVE"));

      // Load all batches in Admin inventory or custody
      const { data: b } = await api.get<ResourceBatch[]>("/api/resources/admin/batches");
      setBatches(b);
    } catch (err) {
      setError(getApiErrorMessage(err, "Failed to load page data."));
    }
  }

  useEffect(() => {
    void loadData();
  }, []);

  function addItemRow() {
    setItems([
      ...items,
      { resourceType: "FOOD", resourceName: "", unit: "units", quantityReceived: 1, expiryDate: "" }
    ]);
  }

  function removeItemRow(index: number) {
    if (items.length > 1) {
      setItems(items.filter((_, i) => i !== index));
    }
  }

  function updateItem(index: number, fields: Partial<LogDonationItem>) {
    setItems(
      items.map((item, i) => (i === index ? { ...item, ...fields } : item))
    );
  }

  async function handleLogDonation(e: FormEvent) {
    e.preventDefault();
    setError("");
    setSuccess("");
    setCreatedRefs([]);
    try {
      const { data } = await api.post<ResourceBatch[]>("/api/resources/batches/bulk", {
        shelterId: shelterId ? Number(shelterId) : null,
        donorEmail: donorEmail,
        items: items.map((item) => ({
          resourceType: item.resourceType,
          resourceName: item.resourceName,
          unit: item.unit,
          quantityReceived: Number(item.quantityReceived),
          expiryDate: item.expiryDate || null
        }))
      });
      const refs = data.map((b) => b.sourceDonationRef);
      setCreatedRefs(refs);
      setSuccess(`Successfully logged ${data.length} donation item(s)!`);
      setItems([
        { resourceType: "FOOD", resourceName: "", unit: "units", quantityReceived: 1, expiryDate: "" }
      ]);
      void loadData();
    } catch (err) {
      setError(getApiErrorMessage(err, "Failed to log bulk donation batch."));
    }
  }

  async function handleTrack(ref: string) {
    setTrackRef(ref);
    setTrackError("");
    setTimelineData(null);
    try {
      const { data } = await api.get<TransparencyView>(`/api/transparency/donations/${ref}`);
      setTimelineData(data);
    } catch (err) {
      setTrackError(getApiErrorMessage(err, "Failed to fetch timeline tracking."));
    }
  }

  async function copyToClipboard(text: string) {
    await navigator.clipboard.writeText(text);
    setSuccess("Tracking reference copied to clipboard!");
    setTimeout(() => setSuccess(""), 2000);
  }

  const selectedShelter = sheltersList.find((s) => s.id === shelterId);

  return (
    <div className="page-grid">
      <SectionCard title="Log Donor Donation">
        <form className="stack" onSubmit={handleLogDonation}>
          <label className="field">
            <span className="field-label required">Select Donor Account</span>
            <select value={donorEmail} onChange={(e) => setDonorEmail(e.target.value)} required>
              {activeDonors.length === 0 ? <option value="">No approved active donors found</option> : null}
              {activeDonors.map((donor) => (
                <option key={donor.id} value={donor.email}>
                  {donor.fullName} ({donor.email})
                </option>
              ))}
            </select>
          </label>

          <label className="field">
            <span className="field-label required">Destination Shelter / Custody</span>
            <select value={shelterId} onChange={(e) => setShelterId(Number(e.target.value))} required>
              <option value={0}>Admin Custody (Hold for manual allocation)</option>
              {sheltersList.map((shelter) => (
                <option key={shelter.id} value={shelter.id}>
                  {shelter.name} ({shelter.district})
                </option>
              ))}
            </select>
          </label>
          {selectedShelter ? (
            <p className="muted" style={{ fontSize: "0.85rem", marginTop: "-0.5rem" }}>
              Selected Shelter Contact: {selectedShelter.contactName} | {selectedShelter.contactPhone}
            </p>
          ) : null}

          <div className="stack" style={{ gap: "1rem", marginTop: "1rem" }}>
            <span style={{ fontWeight: "bold", fontSize: "0.95rem" }}>Donation Items</span>
            {items.map((item, index) => (
              <div key={index} style={{ padding: "0.75rem", border: "1px solid #E5E7EB", borderRadius: "6px", background: "#F9FAFB" }}>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "0.5rem" }}>
                  <span style={{ fontWeight: "bold", fontSize: "0.85rem", color: "#4B5563" }}>Item #{index + 1}</span>
                  {items.length > 1 && (
                    <button type="button" className="secondary-button compact-button" style={{ padding: "2px 8px", fontSize: "0.75rem", color: "#DC2626" }} onClick={() => removeItemRow(index)}>
                      Remove
                    </button>
                  )}
                </div>
                <div className="inline-grid">
                  <label className="field">
                    <span className="field-label required">Resource Type</span>
                    <select value={item.resourceType} onChange={(e) => updateItem(index, { resourceType: e.target.value })}>
                      <option value="FOOD">FOOD</option>
                      <option value="WATER">WATER</option>
                      <option value="MEDICINE">MEDICINE</option>
                      <option value="HYGIENE">HYGIENE</option>
                      <option value="CLOTHING">CLOTHING</option>
                      <option value="OTHER">OTHER</option>
                    </select>
                  </label>
                  <label className="field">
                    <span className="field-label required">Resource Name</span>
                    <input placeholder="Rice (5kg bags)" value={item.resourceName} onChange={(e) => updateItem(index, { resourceName: e.target.value })} required />
                  </label>
                </div>
                <div className="inline-grid">
                  <label className="field">
                    <span className="field-label required">Unit</span>
                    <input placeholder="bags, boxes, bottles" value={item.unit} onChange={(e) => updateItem(index, { unit: e.target.value })} required />
                  </label>
                  <label className="field">
                    <span className="field-label required">Quantity Received</span>
                    <input type="number" min={1} value={item.quantityReceived} onChange={(e) => updateItem(index, { quantityReceived: Number(e.target.value) })} required />
                  </label>
                </div>
                <label className="field">
                  <span className="field-label">Expiry Date</span>
                  <input type="date" value={item.expiryDate} onChange={(e) => updateItem(index, { expiryDate: e.target.value })} />
                </label>
              </div>
            ))}
          </div>

          <button type="button" className="secondary-button" style={{ marginTop: "0.5rem" }} onClick={addItemRow}>
            + Add Another Item
          </button>

          {success ? <p className="success-text" style={{ marginTop: "0.5rem" }}>{success}</p> : null}
          {error ? <p className="error-text" style={{ marginTop: "0.5rem" }}>{error}</p> : null}

          {createdRefs.length > 0 && (
            <div className="stack" style={{ gap: "0.5rem", padding: "0.75rem", background: "#F0F9FF", border: "1px solid #93C5FD", borderRadius: "6px" }}>
              <span style={{ fontWeight: "bold", fontSize: "0.85rem" }}>Created Tracking IDs:</span>
              <div style={{ display: "flex", flexWrap: "wrap", gap: "0.5rem" }}>
                {createdRefs.map((ref) => (
                  <div key={ref} style={{ display: "inline-flex", gap: "0.25rem", alignItems: "center", background: "#EFF6FF", padding: "2px 8px", border: "1px solid #BFDBFE", borderRadius: "4px", fontSize: "0.85rem" }}>
                    <code>{ref}</code>
                    <button type="button" className="secondary-button compact-button" style={{ padding: "0 4px", fontSize: "0.7rem" }} onClick={() => void copyToClipboard(ref)}>Copy</button>
                  </div>
                ))}
              </div>
            </div>
          )}

          <button className="primary-button" type="submit" disabled={activeDonors.length === 0} style={{ marginTop: "1rem" }}>
            Log Donation Batch
          </button>
        </form>
      </SectionCard>

      <div className="stack">
        <SectionCard title="Admin-Held Donation Batches">
          <p className="muted">These items have been donated and are in Admin custody, waiting to be manually transferred to shelters in shortage.</p>
          <div className="table-like" style={{ marginTop: "1rem" }}>
            {batches.map((batch) => (
              <div className="table-row" key={batch.id}>
                <div>
                  <strong>{batch.resourceName}</strong>
                  <p className="muted">{batch.resourceType} | Donor: {batch.donorEmail}</p>
                  <code style={{ fontSize: "0.8rem", color: "#2563EB" }}>{batch.sourceDonationRef}</code>
                </div>
                <div style={{ textAlign: "right" }}>
                  <p style={{ fontWeight: "bold" }}>{batch.quantityAvailable} / {batch.quantityReceived} {batch.unit}</p>
                  <button type="button" className="secondary-button compact-button" style={{ marginTop: "0.25rem" }} onClick={() => void handleTrack(batch.sourceDonationRef)}>
                    Track
                  </button>
                </div>
              </div>
            ))}
            {batches.length === 0 ? <p className="muted">No admin-held batches in inventory.</p> : null}
          </div>
        </SectionCard>

        {trackRef && (
          <SectionCard title={`Tracking History: ${trackRef}`}>
            <button className="secondary-button compact-button" style={{ marginBottom: "1rem" }} onClick={() => setTrackRef(null)}>Close Tracker</button>
            {trackError ? <p className="error-text">{trackError}</p> : null}
            {timelineData ? (
              <div className="stack">
                <p><strong>Resource:</strong> {timelineData.resourceName} ({timelineData.resourceType})</p>
                <p><strong>Quantity:</strong> {timelineData.quantity}</p>
                <div className="timeline">
                  {timelineData.timeline.map((event, idx) => (
                    <div className="timeline-item" key={idx} style={{ paddingLeft: "1.5rem", borderLeft: "2px solid #3B82F6", paddingBottom: "1rem", position: "relative" }}>
                      <div style={{ position: "absolute", left: "-6px", top: "4px", width: "10px", height: "10px", borderRadius: "50%", background: "#3B82F6" }} />
                      <strong style={{ display: "block" }}>{event.eventType}</strong>
                      <p style={{ margin: "0.25rem 0", fontSize: "0.875rem" }}>{event.details}</p>
                      <span className="muted" style={{ fontSize: "0.75rem" }}>{new Date(event.occurredAt).toLocaleString()}</span>
                    </div>
                  ))}
                  {timelineData.timeline.length === 0 ? <p className="muted">No timeline events recorded yet.</p> : null}
                </div>
              </div>
            ) : (
              <p className="muted">Loading tracking data...</p>
            )}
          </SectionCard>
        )}
      </div>
    </div>
  );
}
