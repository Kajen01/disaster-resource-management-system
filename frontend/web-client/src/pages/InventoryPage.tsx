import { FormEvent, useEffect, useState, useMemo } from "react";
import SectionCard from "../components/SectionCard";
import api from "../lib/api";
import { getApiErrorMessage } from "../lib/apiError";
import { useAuth } from "../app/AuthContext";
import type { DonationHistory, ResourceBatch, Shelter, TransparencyView } from "../lib/types";

interface BulkExcessItem {
  batchId: number;
  sourceDonationRef: string;
  resourceType: string;
  resourceName: string;
  unit: string;
  quantity: number;
  maxQuantity: number;
}

export default function InventoryPage() {
  const { auth } = useAuth();
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  // -- Manager States --
  const [lookupShelterId, setLookupShelterId] = useState(0);
  const [managerBatches, setManagerBatches] = useState<ResourceBatch[]>([]);
  const [managedShelters, setManagedShelters] = useState<Shelter[]>([]);
  
  // Bulk excess declaration list in form
  const [bulkItems, setBulkItems] = useState<BulkExcessItem[]>([
    { batchId: 0, sourceDonationRef: "", resourceType: "FOOD", resourceName: "", unit: "units", quantity: 1, maxQuantity: 1 }
  ]);

  // -- Donor States --
  const [donorHistory, setDonorHistory] = useState<DonationHistory | null>(null);

  // Track State for Donor inline tracking lookup
  const [trackRef, setTrackRef] = useState<string | null>(null);
  const [timelineData, setTimelineData] = useState<TransparencyView | null>(null);
  const [trackError, setTrackError] = useState("");

  // -------------------------------------------------------------
  // Data Loaders
  // -------------------------------------------------------------
  async function loadManagerData() {
    try {
      const { data: shelters } = await api.get<Shelter[]>("/api/shelters");
      const mine = shelters.filter((s) => s.managerUserId === auth?.userId);
      setManagedShelters(mine);
      if (mine.length > 0) {
        const activeShelterId = lookupShelterId || mine[0].id;
        setLookupShelterId(activeShelterId);
        // Load inventory for active shelter
        const { data: b } = await api.get<ResourceBatch[]>(`/api/resources/shelters/${activeShelterId}`);
        setManagerBatches(b);
      }
    } catch (err) {
      setError(getApiErrorMessage(err, "Failed to load manager inventory data."));
    }
  }

  async function loadDonorData() {
    try {
      const { data } = await api.get<DonationHistory>("/api/resources/donations/me");
      setDonorHistory(data);
    } catch (err) {
      setError(getApiErrorMessage(err, "Failed to load donor history."));
    }
  }

  useEffect(() => {
    if (auth?.role === "SHELTER_MANAGER") {
      void loadManagerData();
    } else if (auth?.role === "DONOR") {
      void loadDonorData();
    }
  }, [auth?.role, auth?.userId, lookupShelterId]);

  // -------------------------------------------------------------
  // Actions
  // -------------------------------------------------------------
  function addBulkItemRow() {
    setBulkItems([
      ...bulkItems,
      { batchId: 0, sourceDonationRef: "", resourceType: "FOOD", resourceName: "", unit: "units", quantity: 1, maxQuantity: 1 }
    ]);
  }

  function removeBulkItemRow(index: number) {
    if (bulkItems.length > 1) {
      setBulkItems(bulkItems.filter((_, i) => i !== index));
    }
  }

  function updateBulkItem(index: number, fields: Partial<BulkExcessItem>) {
    setBulkItems(
      bulkItems.map((item, i) => (i === index ? { ...item, ...fields } : item))
    );
  }

  async function handlePublishBulkExcess(e: FormEvent) {
    e.preventDefault();
    setError("");
    setSuccess("");
    if (!lookupShelterId) {
      setError("Please select a shelter first.");
      return;
    }
    if (bulkItems.some((item) => !item.batchId)) {
      setError("Please select a valid inventory item for all rows.");
      return;
    }
    try {
      await api.post("/api/shares/excess/bulk", {
        shelterId: lookupShelterId,
        items: bulkItems.map((item) => ({
          batchId: item.batchId,
          sourceDonationRef: item.sourceDonationRef,
          resourceType: item.resourceType,
          resourceName: item.resourceName,
          unit: item.unit,
          quantity: Number(item.quantity)
        }))
      });
      setSuccess(`Declared excess items list successfully published!`);
      // Reset form to single empty item row
      setBulkItems([{ batchId: 0, sourceDonationRef: "", resourceType: "FOOD", resourceName: "", unit: "units", quantity: 1, maxQuantity: 1 }]);
    } catch (err) {
      setError(getApiErrorMessage(err, "Failed to declare bulk excess items."));
    }
  }

  async function handleTrackDonation(ref: string) {
    setTrackRef(ref);
    setTrackError("");
    setTimelineData(null);
    try {
      const { data } = await api.get<TransparencyView>(`/api/transparency/donations/${ref}`);
      setTimelineData(data);
    } catch (err) {
      setTrackError(getApiErrorMessage(err, "Failed to load donation trace."));
    }
  }

  async function copyToClipboard(text: string) {
    await navigator.clipboard.writeText(text);
    setSuccess("Tracking reference copied to clipboard!");
    setTimeout(() => setSuccess(""), 2000);
  }

  // -------------------------------------------------------------
  // Views
  // -------------------------------------------------------------

  // -- DONOR VIEW --
  if (auth?.role === "DONOR") {
    // eslint-disable-next-line react-hooks/rules-of-hooks
    const uniqueDonorBatches = useMemo(() => {
      if (!donorHistory?.batches) return [];
      const seen = new Set<string>();
      return donorHistory.batches.filter((batch: ResourceBatch) => {
        if (!batch.sourceDonationRef) return true;
        if (seen.has(batch.sourceDonationRef)) return false;
        seen.add(batch.sourceDonationRef);
        return true;
      });
    }, [donorHistory?.batches]);

    return (
      <div className="page-grid">
        <SectionCard title="My Donated Items">
          <p className="muted">Below are the items registered under your account by the administrators. Use the Tracking ID to trace their manual transfer path.</p>
          <div className="table-like" style={{ marginTop: "1rem" }}>
            {uniqueDonorBatches.length > 0 ? (
              uniqueDonorBatches.map((batch) => (
                <div className="table-row" key={batch.id}>
                  <div>
                    <strong>{batch.resourceName}</strong>
                    <div style={{ display: "flex", gap: "0.5rem", alignItems: "center" }}>
                      <code style={{ fontSize: "0.85rem", color: "#2563EB" }}>{batch.sourceDonationRef}</code>
                      <button className="secondary-button compact-button" style={{ padding: "1px 6px", fontSize: "0.75rem" }} onClick={() => void copyToClipboard(batch.sourceDonationRef)}>
                        Copy
                      </button>
                    </div>
                  </div>
                  <div style={{ textAlign: "right" }}>
                    <p style={{ fontWeight: "bold" }}>{batch.quantityReceived} {batch.unit}</p>
                    <p className="muted" style={{ fontSize: "0.75rem", marginBottom: "0.25rem" }}>Registered on {new Date(batch.receivedAt).toLocaleDateString()}</p>
                    <button type="button" className="primary-button compact-button" onClick={() => void handleTrackDonation(batch.sourceDonationRef)}>
                      Track Item
                    </button>
                  </div>
                </div>
              ))
            ) : (
              <p className="muted">No donation history recorded. When you hand over goods physically, the Admin will log them here.</p>
            )}
          </div>
        </SectionCard>

        {trackRef && (
          <SectionCard title={`Tracking Timeline: ${trackRef}`}>
            <button className="secondary-button compact-button" style={{ marginBottom: "1rem" }} onClick={() => setTrackRef(null)}>Close Tracker</button>
            {trackError ? <p className="error-text">{trackError}</p> : null}
            {timelineData ? (
              <div className="stack">
                <p><strong>Resource:</strong> {timelineData.resourceName} ({timelineData.resourceType})</p>
                <p><strong>Total Quantity:</strong> {timelineData.quantity}</p>
                <div className="timeline">
                  {timelineData.timeline.map((event, idx) => (
                    <div className="timeline-item" key={idx} style={{ paddingLeft: "1.5rem", borderLeft: "2px solid #3B82F6", paddingBottom: "1rem", position: "relative" }}>
                      <div style={{ position: "absolute", left: "-6px", top: "4px", width: "10px", height: "10px", borderRadius: "50%", background: "#3B82F6" }} />
                      <strong style={{ display: "block" }}>{event.eventType}</strong>
                      <p style={{ margin: "0.25rem 0", fontSize: "0.875rem" }}>{event.details}</p>
                      <span className="muted" style={{ fontSize: "0.75rem" }}>{new Date(event.occurredAt).toLocaleString()}</span>
                    </div>
                  ))}
                  {timelineData.timeline.length === 0 ? <p className="muted">No timeline status updates logged yet.</p> : null}
                </div>
              </div>
            ) : (
              <p className="muted">Fetching tracking logs...</p>
            )}
          </SectionCard>
        )}
      </div>
    );
  }

  // -- SHELTER MANAGER VIEW --
  return (
    <div className="page-grid">
      <SectionCard title="My Shelters">
        <div className="stack">
          {managedShelters.length > 0 ? (
            <div className="table-like">
              {managedShelters.map((shelter) => (
                <div className="table-row" key={shelter.id} style={{ borderLeft: lookupShelterId === shelter.id ? "4px solid #2563EB" : "none" }}>
                  <div>
                    <strong>{shelter.name}</strong>
                    <p className="muted">{shelter.district}</p>
                  </div>
                  <button className={`secondary-button ${lookupShelterId === shelter.id ? "active" : ""}`} onClick={() => setLookupShelterId(shelter.id)}>
                    View Inventory
                  </button>
                </div>
              ))}
            </div>
          ) : (
            <p className="muted">You do not have any registered/approved shelters.</p>
          )}
        </div>
      </SectionCard>

      <div className="stack">
        <SectionCard title="Declare Excess Resources">
          <form className="stack" onSubmit={handlePublishBulkExcess}>
            <p className="muted">Declare items that are currently in excess in your shelter. Other shelters will see them and can request transfers.</p>
            
            <div className="stack" style={{ gap: "1rem", marginTop: "0.5rem" }}>
              {bulkItems.map((item, index) => (
                <div key={index} style={{ padding: "0.75rem", border: "1px solid #E5E7EB", borderRadius: "6px", background: "#F9FAFB" }}>
                  <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "0.5rem" }}>
                    <span style={{ fontWeight: "bold", fontSize: "0.85rem", color: "#4B5563" }}>Item #{index + 1}</span>
                    {bulkItems.length > 1 && (
                      <button type="button" className="secondary-button compact-button" style={{ padding: "2px 8px", fontSize: "0.75rem", color: "#DC2626" }} onClick={() => removeBulkItemRow(index)}>
                        Remove
                      </button>
                    )}
                  </div>
                  <div className="stack" style={{ gap: "0.5rem" }}>
                    <label className="field">
                      <span className="field-label required">Select Inventory Item</span>
                      <select
                        value={item.batchId || ""}
                        onChange={(e) => {
                          const selectedBatchId = Number(e.target.value);
                          const batch = managerBatches.find((b) => b.id === selectedBatchId);
                          if (batch) {
                            updateBulkItem(index, {
                              batchId: batch.id,
                              sourceDonationRef: batch.sourceDonationRef,
                              resourceType: batch.resourceType,
                              resourceName: batch.resourceName,
                              unit: batch.unit,
                              quantity: Math.min(item.quantity, batch.quantityAvailable),
                              maxQuantity: batch.quantityAvailable
                            });
                          } else {
                            updateBulkItem(index, {
                              batchId: 0,
                              sourceDonationRef: "",
                              resourceType: "FOOD",
                              resourceName: "",
                              unit: "units",
                              quantity: 1,
                              maxQuantity: 1
                            });
                          }
                        }}
                        required
                      >
                        <option value="">-- Choose from shelter inventory --</option>
                        {managerBatches.filter(b => b.quantityAvailable > 0).map((b) => (
                          <option key={b.id} value={b.id}>
                            {b.resourceName} ({b.resourceType}) - {b.quantityAvailable} {b.unit} available (ID: {b.id})
                          </option>
                        ))}
                      </select>
                    </label>
                  </div>
                  {item.batchId > 0 && (
                    <div className="inline-grid" style={{ marginTop: "0.5rem" }}>
                      <label className="field">
                        <span className="field-label">Quantity to Declare (Max: {item.maxQuantity})</span>
                        <input
                          type="number"
                          min={1}
                          max={item.maxQuantity}
                          value={item.quantity}
                          onChange={(e) => updateBulkItem(index, { quantity: Number(e.target.value) })}
                          required
                        />
                      </label>
                      <label className="field">
                        <span className="field-label">Unit</span>
                        <input value={item.unit} disabled />
                      </label>
                    </div>
                  )}
                </div>
              ))}
            </div>

            <button type="button" className="secondary-button" style={{ marginTop: "0.5rem" }} onClick={addBulkItemRow}>
              + Add Another Excess Item
            </button>

            {success ? <p className="success-text" style={{ marginTop: "0.5rem" }}>{success}</p> : null}
            {error ? <p className="error-text" style={{ marginTop: "0.5rem" }}>{error}</p> : null}

            <button type="submit" className="primary-button" style={{ marginTop: "1rem" }} disabled={managedShelters.length === 0}>
              Publish Excess List
            </button>
          </form>
        </SectionCard>

        <SectionCard title="Shelter Inventory">
          {managedShelters.length > 0 ? (
            <div>
              <div className="inline-grid" style={{ marginBottom: "1rem" }}>
                <p className="muted">List of available items in the selected shelter's inventory.</p>
                <button className="secondary-button" onClick={() => void loadManagerData()}>Refresh</button>
              </div>

              <div className="table-like">
                {managerBatches.length > 0 ? (
                  managerBatches.map((batch) => (
                    <div className="table-row" key={batch.id}>
                      <div>
                        <strong>{batch.resourceName}</strong>
                        <p className="muted">{batch.resourceType} | {batch.unit} | batch ID: {batch.id}</p>
                        <code style={{ fontSize: "0.8rem" }}>{batch.sourceDonationRef}</code>
                      </div>
                      <div style={{ textAlign: "right" }}>
                        <p style={{ fontWeight: "bold" }}>{batch.quantityAvailable} {batch.unit}</p>
                      </div>
                    </div>
                  ))
                ) : (
                  <p className="muted">No items in this shelter's inventory.</p>
                )}
              </div>
            </div>
          ) : (
            <p className="muted">Select a shelter above to view inventory.</p>
          )}
        </SectionCard>
      </div>
    </div>
  );
}
