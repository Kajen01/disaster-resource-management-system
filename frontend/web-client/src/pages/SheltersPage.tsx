import { FormEvent, useEffect, useState } from "react";
import SectionCard from "../components/SectionCard";
import api from "../lib/api";
import { getApiErrorMessage } from "../lib/apiError";
import { useAuth } from "../app/AuthContext";
import type { Shelter } from "../lib/types";

const initialForm = {
  // Manager User Account
  fullName: "",
  email: "",
  username: "",
  password: "",
  status: "ACTIVE",

  // Shelter Details
  name: "",
  district: "",
  addressLine1: "",
  addressLine2: "",
  contactName: "",
  contactPhone: ""
};

export default function SheltersPage() {
  const { auth } = useAuth();
  const [shelters, setShelters] = useState<Shelter[]>([]);
  const [form, setForm] = useState(initialForm);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  async function loadShelters() {
    try {
      const { data } = await api.get<Shelter[]>("/api/shelters");
      setShelters(data);
    } catch (err) {
      setError(getApiErrorMessage(err, "Could not load shelters."));
    }
  }

  useEffect(() => {
    void loadShelters();
  }, []);

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setError("");
    setSuccess("");
    try {
      await api.post("/api/users", {
        fullName: form.fullName,
        email: form.email,
        username: form.username,
        password: form.password,
        role: "SHELTER_MANAGER",
        status: form.status,
        shelterName: form.name,
        shelterDistrict: form.district,
        shelterAddressLine1: form.addressLine1,
        shelterAddressLine2: form.addressLine2 || undefined,
        shelterContactName: form.contactName,
        shelterContactPhone: form.contactPhone
      });
      setSuccess("Shelter manager and shelter created successfully.");
      setForm(initialForm);
      await loadShelters();
    } catch (err) {
      setError(getApiErrorMessage(err, "Could not create shelter and manager account."));
    }
  }

  const visibleShelters = auth?.role === "SHELTER_MANAGER"
    ? shelters.filter((shelter) => shelter.managerUserId === auth.userId)
    : shelters;

  return (
    <div className="page-grid">
      {auth?.role === "ADMIN" ? (
        <SectionCard title="Register Shelter">
          <form className="stack" onSubmit={onSubmit}>
            <h4 style={{ color: "#2563EB", marginTop: "0.5rem" }}>Shelter Details</h4>
            <label className="field">
              <span className="field-label required">Shelter name</span>
              <input placeholder="Colombo Central Relief Shelter" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
            </label>
            <label className="field">
              <span className="field-label required">District</span>
              <input placeholder="Colombo" value={form.district} onChange={(e) => setForm({ ...form, district: e.target.value })} required />
            </label>
            <label className="field">
              <span className="field-label required">Address line 1</span>
              <input placeholder="123 Relief Road" value={form.addressLine1} onChange={(e) => setForm({ ...form, addressLine1: e.target.value })} required />
            </label>
            <label className="field">
              <span className="field-label">Address line 2</span>
              <input placeholder="Near the municipal playground" value={form.addressLine2} onChange={(e) => setForm({ ...form, addressLine2: e.target.value })} />
            </label>
            <label className="field">
              <span className="field-label required">Primary contact name</span>
              <input placeholder="Ayesha Silva" value={form.contactName} onChange={(e) => setForm({ ...form, contactName: e.target.value })} required />
            </label>
            <label className="field">
              <span className="field-label required">Primary contact phone</span>
              <input placeholder="0771234567" value={form.contactPhone} onChange={(e) => setForm({ ...form, contactPhone: e.target.value })} required />
            </label>

            <h4 style={{ color: "#2563EB", marginTop: "1rem" }}>Manager Account Details</h4>
            <label className="field">
              <span className="field-label required">Manager Full Name</span>
              <input placeholder="Kasuni Fernando" value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} required />
            </label>
            <label className="field">
              <span className="field-label required">Manager Email</span>
              <input type="email" placeholder="kasuni@example.com" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
            </label>
            <label className="field">
              <span className="field-label required">Manager Username</span>
              <input placeholder="kasuni.fernando" value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} required />
            </label>
            <label className="field">
              <span className="field-label required">Manager Password</span>
              <input type="password" placeholder="Minimum 8 characters" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} required />
            </label>

            <label className="field">
              <span className="field-label required">Initial Status</span>
              <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
                <option value="ACTIVE">ACTIVE (Approved)</option>
                <option value="PENDING_APPROVAL">PENDING APPROVAL</option>
                <option value="INACTIVE">INACTIVE</option>
              </select>
            </label>

            {success ? <p className="success-text">{success}</p> : null}
            {error ? <p className="error-text">{error}</p> : null}
            <button className="primary-button" type="submit">Create shelter & manager</button>
          </form>
        </SectionCard>
      ) : (
        <SectionCard title="Managed Shelter Summary">
          <p className="muted">Shelter managers see only shelters assigned to their account.</p>
        </SectionCard>
      )}

      <SectionCard title="Shelter Directory">
        <div className="table-like">
          {visibleShelters.map((shelter) => (
            <div className="table-row" key={shelter.id}>
              <div>
                <strong>{shelter.name}</strong>
                <p className="muted">{shelter.district}</p>
                <p className="muted">Contact: {shelter.contactName} ({shelter.contactPhone})</p>
              </div>
              <div>
                <p className="muted">{shelter.status}</p>
              </div>
            </div>
          ))}
          {visibleShelters.length === 0 ? <p className="muted">No shelters registered.</p> : null}
        </div>
      </SectionCard>
    </div>
  );
}
