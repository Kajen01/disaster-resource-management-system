import { useEffect, useState } from "react";
import SectionCard from "../components/SectionCard";
import api from "../lib/api";
import { useAuth } from "../app/AuthContext";
import type { DonationHistory, ServiceHealth, Shelter, ShortageRequest, UserResponse } from "../lib/types";

export default function OverviewPage() {
  const { auth } = useAuth();
  const [shelters, setShelters] = useState<Shelter[]>([]);
  const [shortages, setShortages] = useState<ShortageRequest[]>([]);
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [health, setHealth] = useState<ServiceHealth[]>([]);
  const [donationHistory, setDonationHistory] = useState<DonationHistory | null>(null);

  useEffect(() => {
    const requests: Promise<unknown>[] = [
      api.get<Shelter[]>("/api/shelters").then((response) => setShelters(response.data)).catch(() => undefined)
    ];
    if (auth?.role === "ADMIN") {
      requests.push(api.get<UserResponse[]>("/api/users").then((response) => setUsers(response.data)).catch(() => undefined));
      requests.push(api.get<ServiceHealth[]>("/api/admin/health").then((response) => setHealth(response.data)).catch(() => undefined));
    }
    if (auth?.role === "DONOR") {
      requests.push(api.get<DonationHistory>("/api/resources/donations/me").then((response) => setDonationHistory(response.data)).catch(() => undefined));
    }
    void Promise.all(requests);
  }, [auth]);

  const managedShelters = shelters.filter((shelter) => shelter.managerUserId === auth?.userId);

  return (
    <div className="page-grid">
      <SectionCard title="Operational Snapshot">
        <div className="stats-grid">
          <div className="stat">
            <span>Total shelters</span>
            <strong>{shelters.length}</strong>
          </div>
          <div className="stat">
            <span>Role</span>
            <strong className="stat-value-wrap">{auth?.role ?? "Guest"}</strong>
          </div>
          <div className="stat">
            <span>
              {auth?.role === "ADMIN"
                ? "Registered users"
                : auth?.role === "SHELTER_MANAGER"
                  ? "Managed shelters"
                  : "Donation batches"}
            </span>
            <strong>
              {auth?.role === "ADMIN"
                ? users.length
                : auth?.role === "SHELTER_MANAGER"
                  ? managedShelters.length
                  : (donationHistory?.totalBatches ?? 0)}
            </strong>
          </div>
        </div>
      </SectionCard>
      <SectionCard title="Session">
        <div className="stack">
          <p><strong>Name:</strong> {auth?.fullName}</p>
          <p><strong>Email:</strong> {auth?.email}</p>
          <p><strong>Role:</strong> {auth?.role}</p>
          <p><strong>Status:</strong> {auth?.status}</p>
        </div>
      </SectionCard>
      {auth?.role === "ADMIN" ? (
        <SectionCard title="Admin Highlights">
          <div className="stack">
            <p><strong>Active users:</strong> {users.filter((user) => user.status === "ACTIVE").length}</p>
            <p><strong>Healthy services:</strong> {health.filter((item) => item.reachable && item.status === "UP").length}</p>
            <p><strong>Inactive shelters:</strong> {shelters.filter((shelter) => shelter.status === "INACTIVE").length}</p>
          </div>
        </SectionCard>
      ) : null}
      {auth?.role === "SHELTER_MANAGER" ? (
        <SectionCard title="Manager Highlights">
          <div className="stack">
            <p><strong>Your shelters:</strong> {managedShelters.map((shelter) => shelter.name).join(", ") || "None assigned yet"}</p>
            <p><strong>Active shelters:</strong> {managedShelters.filter((s) => s.status === "ACTIVE").length}</p>
          </div>
        </SectionCard>
      ) : null}
      {auth?.role === "DONOR" ? (
        <SectionCard title="Donor Highlights">
          <div className="stack">
            <p><strong>Your batches:</strong> {donationHistory?.totalBatches ?? 0}</p>
            <p><strong>Tracked donor email:</strong> {donationHistory?.donorEmail ?? auth.email}</p>
          </div>
        </SectionCard>
      ) : null}
    </div>
  );
}
