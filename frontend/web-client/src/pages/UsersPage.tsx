import { FormEvent, useEffect, useMemo, useState } from "react";
import SectionCard from "../components/SectionCard";
import api from "../lib/api";
import { getApiErrorMessage } from "../lib/apiError";
import type { Role, UserResponse, UserStatus, Shelter } from "../lib/types";

const initialForm = {
  fullName: "",
  email: "",
  username: "",
  password: "",
  role: "DONOR" as Role,
  status: "ACTIVE" as UserStatus,
  shelterName: "",
  shelterDistrict: "",
  shelterAddressLine1: "",
  shelterAddressLine2: "",
  shelterContactName: "",
  shelterContactPhone: ""
};

export default function UsersPage() {
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [shelters, setShelters] = useState<Shelter[]>([]);
  const [form, setForm] = useState(initialForm);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  async function loadUsers() {
    const { data } = await api.get<UserResponse[]>("/api/users");
    setUsers(data);
  }

  async function loadShelters() {
    try {
      const { data } = await api.get<Shelter[]>("/api/shelters");
      setShelters(data);
    } catch (err) {
      console.error("Failed to load shelters", err);
    }
  }

  async function updateStatus(id: number, status: UserStatus) {
    setError("");
    setSuccess("");
    try {
      await api.patch(`/api/users/${id}/status`, { status });
      setSuccess("User status updated successfully.");
      await loadUsers();
      await loadShelters();
    } catch (err) {
      setError(getApiErrorMessage(err, "Could not update the user status."));
    }
  }

  async function createUser(event: FormEvent) {
    event.preventDefault();
    setError("");
    setSuccess("");
    try {
      const payload = {
        fullName: form.fullName,
        email: form.email,
        username: form.username,
        password: form.password,
        role: form.role,
        status: form.status,
        ...(form.role === "SHELTER_MANAGER" ? {
          shelterName: form.shelterName,
          shelterDistrict: form.shelterDistrict,
          shelterAddressLine1: form.shelterAddressLine1,
          shelterAddressLine2: form.shelterAddressLine2 || undefined,
          shelterContactName: form.shelterContactName,
          shelterContactPhone: form.shelterContactPhone
        } : {})
      };
      await api.post("/api/users", payload);
      setSuccess("User created successfully.");
      setForm(initialForm);
      await loadUsers();
      await loadShelters();
    } catch (err) {
      setError(getApiErrorMessage(err, "Could not create the user."));
    }
  }

  useEffect(() => {
    void loadUsers();
    void loadShelters();
  }, []);

  const pendingUsers = useMemo(
    () => users.filter((user) => user.status === "PENDING_APPROVAL"),
    [users]
  );

  return (
    <div className="page-grid">
      <SectionCard title="Create User">
        <form className="stack" onSubmit={createUser}>
          <label className="field">
            <span className="field-label required">Full name</span>
            <input
              value={form.fullName}
              onChange={(event) => setForm({ ...form, fullName: event.target.value })}
              placeholder="Kasuni Fernando"
              required
            />
          </label>
          <label className="field">
            <span className="field-label required">Email address</span>
            <input
              value={form.email}
              onChange={(event) => setForm({ ...form, email: event.target.value })}
              type="email"
              placeholder="kasuni@example.com"
              required
            />
          </label>
          <label className="field">
            <span className="field-label required">Username</span>
            <input
              value={form.username}
              onChange={(event) => setForm({ ...form, username: event.target.value })}
              placeholder="kasuni.fernando"
              required
            />
          </label>
          <label className="field">
            <span className="field-label required">Temporary password</span>
            <input
              value={form.password}
              onChange={(event) => setForm({ ...form, password: event.target.value })}
              type="password"
              placeholder="Minimum 8 characters"
              required
            />
          </label>
          <div className="inline-grid">
            <label className="field">
              <span className="field-label required">User role</span>
              <select value={form.role} onChange={(event) => setForm({ ...form, role: event.target.value as Role })}>
                <option value="DONOR">Donor</option>
                <option value="SHELTER_MANAGER">Shelter Manager</option>
                <option value="ADMIN">Admin</option>
              </select>
            </label>
            <label className="field">
              <span className="field-label required">Initial account status</span>
              <select value={form.status} onChange={(event) => setForm({ ...form, status: event.target.value as UserStatus })}>
                <option value="ACTIVE">Active</option>
                <option value="PENDING_APPROVAL">Pending Approval</option>
                <option value="INACTIVE">Inactive</option>
                <option value="SUSPENDED">Suspended</option>
              </select>
            </label>
          </div>

          {form.role === "SHELTER_MANAGER" ? (
            <div className="shelter-subform">
              <h4 style={{ color: "#2563EB", margin: "0.5rem 0" }}>Shelter Details</h4>
              <label className="field">
                <span className="field-label required">Shelter Name</span>
                <input value={form.shelterName} onChange={(event) => setForm({ ...form, shelterName: event.target.value })} placeholder="Colombo Central Shelter" required />
              </label>
              <label className="field">
                <span className="field-label required">District</span>
                <input value={form.shelterDistrict} onChange={(event) => setForm({ ...form, shelterDistrict: event.target.value })} placeholder="Colombo" required />
              </label>
              <label className="field">
                <span className="field-label required">Address Line 1</span>
                <input value={form.shelterAddressLine1} onChange={(event) => setForm({ ...form, shelterAddressLine1: event.target.value })} placeholder="123 Galle Road" required />
              </label>
              <label className="field">
                <span className="field-label">Address Line 2</span>
                <input value={form.shelterAddressLine2} onChange={(event) => setForm({ ...form, shelterAddressLine2: event.target.value })} placeholder="Suite 4" />
              </label>
              <label className="field">
                <span className="field-label required">Contact Name</span>
                <input value={form.shelterContactName} onChange={(event) => setForm({ ...form, shelterContactName: event.target.value })} placeholder="Saman Perera" required />
              </label>
              <label className="field">
                <span className="field-label required">Contact Phone</span>
                <input value={form.shelterContactPhone} onChange={(event) => setForm({ ...form, shelterContactPhone: event.target.value })} placeholder="0771234567" required />
              </label>
            </div>
          ) : null}

          <p className="muted">Admin-created users can be created directly as active, or held for later approval if needed.</p>
          {success ? <p className="success-text">{success}</p> : null}
          {error ? <p className="error-text">{error}</p> : null}
          <button className="primary-button" type="submit">Create user</button>
        </form>
      </SectionCard>

      <div className="stack">
        <SectionCard title="Pending Approvals">
          {pendingUsers.length === 0 ? <p className="muted">No users are waiting for approval right now.</p> : null}
          <div className="table-like">
            {pendingUsers.map((user) => (
              <div className="table-row" key={user.id}>
                <div>
                  <strong>{user.fullName}</strong>
                  <p className="muted">{user.email} | {user.role}</p>
                </div>
                <button className="primary-button" onClick={() => void updateStatus(user.id, "ACTIVE")}>
                  Approve account
                </button>
              </div>
            ))}
          </div>
        </SectionCard>
        
        <SectionCard title="User Management">
          <div className="table-like">
            {users.map((user) => (
              <div className="table-row" key={user.id}>
                <div>
                  <strong>{user.fullName}</strong>
                  <p className="muted">{user.email} | {user.role}</p>
                </div>
                <div className="inline-grid compact">
                  <label className="field">
                    <span className="field-label required">Account status</span>
                    <select value={user.status} onChange={(event) => void updateStatus(user.id, event.target.value as UserStatus)}>
                      <option value="PENDING_APPROVAL">PENDING_APPROVAL</option>
                      <option value="ACTIVE">ACTIVE</option>
                      <option value="INACTIVE">INACTIVE</option>
                      <option value="SUSPENDED">SUSPENDED</option>
                    </select>
                  </label>
                </div>
              </div>
            ))}
          </div>
        </SectionCard>

        <SectionCard title="Registered Shelters">
          <div className="table-like">
            {shelters.map((shelter) => (
              <div className="table-row" key={shelter.id}>
                <div>
                  <strong>{shelter.name}</strong>
                  <p className="muted">{shelter.district} | Manager ID: {shelter.managerUserId}</p>
                  <p className="muted">Contact: {shelter.contactName} ({shelter.contactPhone})</p>
                </div>
                <div style={{ textAlign: "right" }}>
                  <span style={{ fontSize: "0.8rem", padding: "4px 8px", borderRadius: "4px", background: shelter.status === "ACTIVE" ? "#DEF7EC" : "#FDE8E8", color: shelter.status === "ACTIVE" ? "#03543F" : "#9B1C1C" }}>
                    {shelter.status}
                  </span>
                </div>
              </div>
            ))}
            {shelters.length === 0 ? <p className="muted">No registered shelters found.</p> : null}
          </div>
        </SectionCard>
      </div>
    </div>
  );
}
