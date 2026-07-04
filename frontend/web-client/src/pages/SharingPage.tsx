import { FormEvent, useEffect, useState, useMemo } from "react";
import SectionCard from "../components/SectionCard";
import api from "../lib/api";
import { getApiErrorMessage } from "../lib/apiError";
import { useAuth } from "../app/AuthContext";
import type { Shelter, ShortageRequest, Transfer, ResourceBatch } from "../lib/types";

// Custom type definitions for Sharing Page
type ExcessNotification = {
  id: number;
  shelterId: number;
  resourceType: string;
  resourceName: string;
  unit: string;
  quantity: number;
  status: string;
  createdAt: string;
};

type ExcessRequest = {
  id: number;
  excessNotificationId: number;
  requestingShelterId: number;
  quantity: number;
  status: string;
  createdAt: string;
};

export default function SharingPage() {
  const { auth } = useAuth();
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  // -- Common Lists --
  const [allShelters, setAllShelters] = useState<Shelter[]>([]);
  const [allShortages, setAllShortages] = useState<ShortageRequest[]>([]);
  const [allTransfers, setAllTransfers] = useState<Transfer[]>([]);

  // -- Manager Selection --
  const [activeShelterId, setActiveShelterId] = useState(0);

  // -- Admin State --
  const [adminBatches, setAdminBatches] = useState<ResourceBatch[]>([]);
  const [adminTransferForm, setAdminTransferForm] = useState({
    show: false,
    shortageId: null as number | null,
    targetShelterId: 0,
    batchId: "",
    quantity: 1,
    donationRef: "",
    resourceName: "",
    resourceType: "",
    unit: "",
    maxQuantity: 1
  });

  // -- Excess Notifications & Requests --
  const [excessNotifications, setExcessNotifications] = useState<ExcessNotification[]>([]);
  const [requestsForMyExcess, setRequestsForMyExcess] = useState<ExcessRequest[]>([]);
  const [excessRequestForm, setExcessRequestForm] = useState({
    show: false,
    notificationId: 0,
    resourceName: "",
    maxQuantity: 1,
    quantity: 1
  });

  // -- Shortage Form --
  const [shortageForm, setShortageForm] = useState({
    resourceType: "FOOD",
    resourceName: "",
    unit: "units",
    requiredQuantity: 1,
    justification: ""
  });

  // -------------------------------------------------------------
  // Data Fetching
  // -------------------------------------------------------------
  async function loadData() {
    try {
      const { data: shelters } = await api.get<Shelter[]>("/api/shelters");
      setAllShelters(shelters);

      // Load shortages
      const { data: shortages } = await api.get<ShortageRequest[]>("/api/transparency/shortages");
      setAllShortages(shortages);

      // Filter/find manager active shelter
      if (auth?.role === "SHELTER_MANAGER" && shelters.length > 0) {
        const mine = shelters.filter((s) => s.managerUserId === auth.userId);
        if (mine.length > 0 && !activeShelterId) {
          setActiveShelterId(mine[0].id);
        }
      }

      // Load excess notifications
      const { data: excess } = await api.get<ExcessNotification[]>("/api/shares/excess");
      setExcessNotifications(excess);

      if (auth?.role === "ADMIN") {
        const { data: batches } = await api.get<ResourceBatch[]>("/api/resources/admin/batches");
        setAdminBatches(batches);
      }
    } catch (err) {
      setError(getApiErrorMessage(err, "Failed to load data."));
    }
  }

  async function loadTransfers() {
    if (!activeShelterId) return;
    try {
      const { data } = await api.get<Transfer[]>(`/api/shares/transfers?shelterId=${activeShelterId}&direction=all`);
      setAllTransfers(data);
    } catch (err) {
      setError(getApiErrorMessage(err, "Failed to load transfers."));
    }
  }

  // Load excess requests if manager has published notifications
  async function loadExcessRequests() {
    try {
      const myShelterIds = allShelters
        .filter((s) => s.managerUserId === auth?.userId)
        .map((s) => s.id);
      
      const { data: excessList } = await api.get<ExcessNotification[]>("/api/shares/excess");
      const myNotifications = excessList.filter((e) => myShelterIds.includes(e.shelterId));

      const requests: ExcessRequest[] = [];
      for (const notif of myNotifications) {
        const { data: reqs } = await api.get<ExcessRequest[]>(`/api/shares/excess/${notif.id}/requests`);
        requests.push(...reqs.filter((r) => r.status === "PENDING"));
      }
      setRequestsForMyExcess(requests);
    } catch (err) {
      console.error("Failed to load excess requests", err);
    }
  }

  useEffect(() => {
    void loadData();
  }, [auth]);

  useEffect(() => {
    if (activeShelterId) {
      void loadTransfers();
      void loadExcessRequests();
    }
  }, [activeShelterId, allShelters]);

  // -------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------
  const myShelters = useMemo(() => {
    return allShelters.filter((s) => s.managerUserId === auth?.userId);
  }, [allShelters, auth]);

  function getShelterName(shelterId: number) {
    if (shelterId === 0) return "Admin (Donor Donations)";
    return allShelters.find((shelter) => shelter.id === shelterId)?.name ?? `Shelter #${shelterId}`;
  }

  // -------------------------------------------------------------
  // Form Submissions & Actions
  // -------------------------------------------------------------
  async function handleCreateShortage(e: FormEvent) {
    e.preventDefault();
    setError("");
    setSuccess("");
    try {
      await api.post("/api/shares/requests", {
        ...shortageForm,
        shelterId: activeShelterId
      });
      setSuccess("Shortage request submitted successfully.");
      setShortageForm({
        resourceType: "FOOD",
        resourceName: "",
        unit: "units",
        requiredQuantity: 1,
        justification: ""
      });
      void loadData();
    } catch (err) {
      setError(getApiErrorMessage(err, "Failed to submit shortage request."));
    }
  }

  async function handleAdminTransferSubmit(e: FormEvent) {
    e.preventDefault();
    setError("");
    setSuccess("");
    try {
      const selectedBatch = adminBatches.find((b) => b.id === Number(adminTransferForm.batchId));
      if (!selectedBatch) {
        setError("Please select a valid donation batch.");
        return;
      }
      await api.post("/api/shares/transfers/admin-transfer", {
        shortageRequestId: adminTransferForm.shortageId,
        targetShelterId: adminTransferForm.targetShelterId,
        resourceType: selectedBatch.resourceType,
        resourceName: selectedBatch.resourceName,
        unit: selectedBatch.unit,
        quantity: Number(adminTransferForm.quantity),
        batchId: selectedBatch.id,
        donationRef: selectedBatch.sourceDonationRef
      });
      setSuccess("Manual dispatch logged and transfer marked as Dispatched!");
      setAdminTransferForm({
        show: false,
        shortageId: null,
        targetShelterId: 0,
        batchId: "",
        quantity: 1,
        donationRef: "",
        resourceName: "",
        resourceType: "",
        unit: "",
        maxQuantity: 1
      });
      void loadData();
    } catch (err) {
      setError(getApiErrorMessage(err, "Failed to dispatch admin transfer."));
    }
  }

  async function handleRequestExcessSubmit(e: FormEvent) {
    e.preventDefault();
    setError("");
    setSuccess("");
    try {
      await api.post(`/api/shares/excess/${excessRequestForm.notificationId}/requests`, {
        requestingShelterId: activeShelterId,
        quantity: excessRequestForm.quantity
      });
      setSuccess("Excess request submitted to the shelter manager!");
      setExcessRequestForm({
        show: false,
        notificationId: 0,
        resourceName: "",
        maxQuantity: 1,
        quantity: 1
      });
      void loadData();
    } catch (err) {
      setError(getApiErrorMessage(err, "Failed to request excess."));
    }
  }

  async function handleApproveExcessRequest(requestId: number) {
    setError("");
    setSuccess("");
    try {
      await api.patch(`/api/shares/excess/requests/${requestId}/approve`);
      setSuccess("Request approved! Items marked as Dispatched.");
      void loadData();
      void loadExcessRequests();
      void loadTransfers();
    } catch (err) {
      setError(getApiErrorMessage(err, "Failed to approve request."));
    }
  }

  async function handleRejectExcessRequest(requestId: number) {
    setError("");
    setSuccess("");
    try {
      await api.patch(`/api/shares/excess/requests/${requestId}/reject`);
      setSuccess("Request rejected.");
      void loadData();
      void loadExcessRequests();
    } catch (err) {
      setError(getApiErrorMessage(err, "Failed to reject request."));
    }
  }

  async function handleReceiveTransfer(transferId: number) {
    setError("");
    setSuccess("");
    try {
      await api.post(`/api/shares/transfers/${transferId}/receive`);
      setSuccess("Arrival confirmed! Items successfully added to your inventory.");
      void loadData();
      void loadTransfers();
    } catch (err) {
      setError(getApiErrorMessage(err, "Failed to confirm transfer receipt."));
    }
  }

  // -------------------------------------------------------------
  // Views
  // -------------------------------------------------------------

  // -- DONOR VIEW --
  if (auth?.role === "DONOR") {
    return (
      <div className="page-grid">
        <SectionCard title="Shortage Board">
          <p className="muted">Review existing shelter shortage requests. Donor donations are handed to the main Admin, who manually allocates items to these needs.</p>
          <div className="table-like" style={{ marginTop: "1rem" }}>
            {allShortages.length > 0 ? (
              allShortages.map((item) => (
                <div className="table-row summary-row" key={item.id}>
                  <div>
                    <strong>{item.resourceName}</strong>
                    <p className="muted">{item.resourceType} | {item.unit}</p>
                    <p className="muted">Shelter: {getShelterName(item.shelterId)}</p>
                    <p className="muted">Justification: {item.justification || "No justification provided"}</p>
                  </div>
                  <div className="summary-meta" style={{ textAlign: "right" }}>
                    <p><strong>Quantity:</strong> {item.shortageQuantity}</p>
                    <p><strong>Status:</strong> {item.status}</p>
                  </div>
                </div>
              ))
            ) : (
              <p className="muted">No shortage requests currently open.</p>
            )}
          </div>
        </SectionCard>
      </div>
    );
  }

  // -- ADMIN VIEW --
  if (auth?.role === "ADMIN") {
    return (
      <div className="page-grid">
        {adminTransferForm.show && (
          <SectionCard title="Allocate & Transfer Donor Goods">
            <form className="stack" onSubmit={handleAdminTransferSubmit}>
              <p className="muted">
                Transferring items manually to <strong>{getShelterName(adminTransferForm.targetShelterId)}</strong>.
              </p>
              <label className="field">
                <span className="field-label required">Select Donor Batch (Admin-Held)</span>
                <select value={adminTransferForm.batchId} onChange={(e) => {
                  const b = adminBatches.find((item) => item.id === Number(e.target.value));
                  setAdminTransferForm({
                    ...adminTransferForm,
                    batchId: e.target.value,
                    maxQuantity: b ? b.quantityAvailable : 1,
                    quantity: b ? Math.min(adminTransferForm.quantity, b.quantityAvailable) : 1
                  });
                }} required>
                  <option value="">-- Choose Batch --</option>
                  {adminBatches.map((b) => (
                    <option key={b.id} value={b.id}>
                      {b.resourceName} (Avail: {b.quantityAvailable} {b.unit}) [Ref: {b.sourceDonationRef}]
                    </option>
                  ))}
                </select>
              </label>

              <label className="field">
                <span className="field-label required">Quantity to Transfer</span>
                <input type="number" min={1} value={adminTransferForm.quantity} onChange={(e) => setAdminTransferForm({ ...adminTransferForm, quantity: Number(e.target.value) })} required />
              </label>

              <div style={{ display: "flex", gap: "0.5rem" }}>
                <button type="submit" className="primary-button compact-button">Dispatch Transfer</button>
                <button type="button" className="secondary-button compact-button" onClick={() => setAdminTransferForm({ ...adminTransferForm, show: false })}>Cancel</button>
              </div>
            </form>
          </SectionCard>
        )}

        <SectionCard title="Shelter Shortage Requests">
          <p className="muted">Review shortages declared by shelters, and manually transfer matching admin-held batches.</p>
          {success ? <p className="success-text" style={{ margin: "1rem 0" }}>{success}</p> : null}
          {error ? <p className="error-text" style={{ margin: "1rem 0" }}>{error}</p> : null}
          <div className="table-like" style={{ marginTop: "1rem" }}>
            {allShortages.length > 0 ? (
              allShortages.map((item) => (
                <div className="table-row summary-row" key={item.id}>
                  <div>
                    <strong>{item.resourceName}</strong>
                    <p className="muted">{item.resourceType} | {item.unit}</p>
                    <p className="muted">Shelter: {getShelterName(item.shelterId)}</p>
                    <p className="muted">Justification: {item.justification || "No justification provided"}</p>
                  </div>
                  <div className="summary-meta" style={{ textAlign: "right" }}>
                    <p><strong>Quantity Needed:</strong> {item.shortageQuantity}</p>
                    <p style={{ fontWeight: "bold", color: item.status === "OPEN" ? "#EAB308" : "#10B981" }}>Status: {item.status}</p>
                    {item.status === "OPEN" && (
                      <button className="primary-button compact-button" style={{ marginTop: "0.5rem" }} onClick={() => setAdminTransferForm({
                        show: true,
                        shortageId: item.id,
                        targetShelterId: item.shelterId,
                        batchId: "",
                        quantity: item.shortageQuantity,
                        donationRef: "",
                        resourceName: item.resourceName,
                        resourceType: item.resourceType,
                        unit: item.unit,
                        maxQuantity: 1
                      })}>
                        Allocate & Transfer
                      </button>
                    )}
                  </div>
                </div>
              ))
            ) : (
              <p className="muted">No shelter shortage requests registered.</p>
            )}
          </div>
        </SectionCard>
      </div>
    );
  }

  // -- SHELTER MANAGER VIEW --
  return (
    <div className="page-grid">
      <div style={{ gridColumn: "1 / -1", display: "flex", justifyContent: "flex-end" }}>
        <button className="secondary-button" onClick={() => { void loadData(); void loadTransfers(); void loadExcessRequests(); }}>Refresh Data</button>
      </div>

      {/* Shortage Form */}
      <SectionCard title="Report Resource Shortage">
        <form className="stack" onSubmit={handleCreateShortage}>
          <div className="inline-grid">
            <label className="field">
              <span className="field-label required">Category</span>
              <select value={shortageForm.resourceType} onChange={(e) => setShortageForm({ ...shortageForm, resourceType: e.target.value })}>
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
              <input placeholder="Blankets" value={shortageForm.resourceName} onChange={(e) => setShortageForm({ ...shortageForm, resourceName: e.target.value })} required />
            </label>
          </div>
          <div className="inline-grid">
            <label className="field">
              <span className="field-label required">Unit</span>
              <input placeholder="pieces, packs, boxes" value={shortageForm.unit} onChange={(e) => setShortageForm({ ...shortageForm, unit: e.target.value })} required />
            </label>
            <label className="field">
              <span className="field-label required">Quantity Needed</span>
              <input type="number" min={1} value={shortageForm.requiredQuantity} onChange={(e) => setShortageForm({ ...shortageForm, requiredQuantity: Number(e.target.value) })} required />
            </label>
          </div>
          <label className="field">
            <span className="field-label">Justification</span>
            <textarea placeholder="Reason for shortage..." value={shortageForm.justification} onChange={(e) => setShortageForm({ ...shortageForm, justification: e.target.value })} />
          </label>

          {success ? <p className="success-text">{success}</p> : null}
          {error ? <p className="error-text">{error}</p> : null}
          <button className="primary-button" type="submit" disabled={myShelters.length === 0}>Submit Shortage</button>
        </form>
      </SectionCard>

      {/* Share Excess Form Modal */}
      {excessRequestForm.show && (
        <div style={{ gridColumn: "1 / -1" }}>
          <SectionCard title="Request Excess Resources">
            <form className="stack" onSubmit={handleRequestExcessSubmit}>
              <p className="muted">Requesting item: {excessRequestForm.resourceName}</p>
              <label className="field">
                <span className="field-label required">Quantity to Request</span>
                <input type="number" min={1} max={excessRequestForm.maxQuantity} value={excessRequestForm.quantity} onChange={(e) => setExcessRequestForm({ ...excessRequestForm, quantity: Math.min(excessRequestForm.maxQuantity, Number(e.target.value)) })} required />
                <span className="muted" style={{ fontSize: "0.75rem" }}>Max available: {excessRequestForm.maxQuantity}</span>
              </label>
              <div style={{ display: "flex", gap: "0.5rem" }}>
                <button type="submit" className="primary-button compact-button">Submit Request</button>
                <button type="button" className="secondary-button compact-button" onClick={() => setExcessRequestForm({ ...excessRequestForm, show: false })}>Cancel</button>
              </div>
            </form>
          </SectionCard>
        </div>
      )}

      {/* Excess Sharing Notifications Board */}
      <SectionCard title="Excess Resources Board">
        <p className="muted">Available resources published by other shelters. You can request these directly.</p>
        <div className="table-like" style={{ marginTop: "1rem" }}>
          {excessNotifications.filter((e) => e.shelterId !== activeShelterId).length > 0 ? (
            excessNotifications
              .filter((e) => e.shelterId !== activeShelterId)
              .map((item) => (
                <div className="table-row" key={item.id}>
                  <div>
                    <strong>{item.resourceName}</strong>
                    <p className="muted">Available: {item.quantity} {item.unit} | {item.resourceType}</p>
                    <p className="muted">From: {getShelterName(item.shelterId)}</p>
                  </div>
                  {!excessRequestForm.show && (
                    <button className="primary-button compact-button" onClick={() => setExcessRequestForm({
                      show: true,
                      notificationId: item.id,
                      resourceName: item.resourceName,
                      maxQuantity: item.quantity,
                      quantity: item.quantity
                    })}>
                      Request Items
                    </button>
                  )}
                </div>
              ))
          ) : (
            <p className="muted">No excess notifications from other shelters.</p>
          )}
        </div>
      </SectionCard>

      {/* Requests Made for My Excess */}
      <SectionCard title="Requests for My Excess Items">
        <p className="muted">Incoming requests from other shelters for excess items you published.</p>
        <div className="table-like" style={{ marginTop: "1rem" }}>
          {requestsForMyExcess.length > 0 ? (
            requestsForMyExcess.map((req) => {
              const matchingNotif = excessNotifications.find((n) => n.id === req.excessNotificationId);
              return (
                <div className="table-row" key={req.id}>
                  <div>
                    <strong>{matchingNotif ? matchingNotif.resourceName : "Resource"}</strong>
                    <p className="muted">Requested Quantity: {req.quantity}</p>
                    <p className="muted">From Shelter: {getShelterName(req.requestingShelterId)}</p>
                  </div>
                  <div style={{ display: "flex", gap: "0.25rem" }}>
                    <button className="primary-button compact-button" onClick={() => void handleApproveExcessRequest(req.id)}>Approve & Dispatch</button>
                    <button className="secondary-button compact-button" onClick={() => void handleRejectExcessRequest(req.id)}>Reject</button>
                  </div>
                </div>
              );
            })
          ) : (
            <p className="muted">No pending requests for your excess notifications.</p>
          )}
        </div>
      </SectionCard>

      {/* Transfers and Confirm Arrival Dashboard */}
      <SectionCard title="My Transfers (Incoming / Outgoing)">
        <p className="muted">Confirm manual receipt of incoming items once they arrive physically.</p>
        <div className="table-like" style={{ marginTop: "1rem" }}>
          {allTransfers.length > 0 ? (
            allTransfers.map((item) => {
              const isIncoming = item.targetShelterId === activeShelterId;
              return (
                <div className="table-row" key={item.transferId}>
                  <div>
                    <strong>{item.resourceName}</strong>
                    <p className="muted">Quantity: {item.quantity} {item.unit}</p>
                    <p className="muted">{isIncoming ? `Incoming from: ${getShelterName(item.sourceShelterId)}` : `Outgoing to: ${getShelterName(item.targetShelterId)}`}</p>
                    <code>Tracking ID: {item.donationRef}</code>
                  </div>
                  <div style={{ textAlign: "right", display: "flex", flexDirection: "column", gap: "0.5rem", alignItems: "flex-end" }}>
                    <span style={{ fontSize: "0.85rem", fontWeight: "bold" }}>{item.status}</span>
                    {isIncoming && item.status === "DISPATCHED" && (
                      <button className="primary-button compact-button" onClick={() => void handleReceiveTransfer(item.transferId)}>
                        Confirm Arrival
                      </button>
                    )}
                  </div>
                </div>
              );
            })
          ) : (
            <p className="muted">No transfers logged for this shelter.</p>
          )}
        </div>
      </SectionCard>
    </div>
  );
}
