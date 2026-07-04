import { FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../lib/api";
import { getApiErrorMessage } from "../lib/apiError";
import { useAuth } from "../app/AuthContext";
import type { RegistrationResponse, Role } from "../lib/types";

export default function RegisterPage() {
  const navigate = useNavigate();
  const { setAuth } = useAuth();
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [form, setForm] = useState({
    fullName: "",
    email: "",
    username: "",
    password: "",
    role: "DONOR" as Role,
    shelterName: "",
    shelterDistrict: "",
    shelterAddressLine1: "",
    shelterAddressLine2: "",
    shelterContactName: "",
    shelterContactPhone: ""
  });

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setError("");
    setSuccess("");
    try {
      // Prepare payload: only send shelter fields if role is SHELTER_MANAGER
      const payload = {
        fullName: form.fullName,
        email: form.email,
        username: form.username,
        password: form.password,
        role: form.role,
        ...(form.role === "SHELTER_MANAGER" ? {
          shelterName: form.shelterName,
          shelterDistrict: form.shelterDistrict,
          shelterAddressLine1: form.shelterAddressLine1,
          shelterAddressLine2: form.shelterAddressLine2 || undefined,
          shelterContactName: form.shelterContactName,
          shelterContactPhone: form.shelterContactPhone
        } : {})
      };
      const { data } = await api.post<RegistrationResponse>("/api/auth/register", payload);
      setSuccess(data.message);
      if (data.approved && data.token) {
        setAuth({
          userId: data.userId,
          fullName: data.fullName,
          email: data.email,
          username: data.username,
          role: data.role,
          status: data.status,
          token: data.token
        });
        navigate("/");
        return;
      }
      setTimeout(() => navigate("/login"), 1800);
    } catch (err) {
      setError(getApiErrorMessage(err, "Registration failed. Please review the form and try again."));
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-form" onSubmit={onSubmit}>
        <p className="eyebrow">New User</p>
        <h1>Create DRMS account</h1>
        <label>
          <span className="field-label required">Full name</span>
          <input value={form.fullName} onChange={(event) => setForm({ ...form, fullName: event.target.value })} placeholder="Nimal Perera" required />
        </label>
        <label>
          <span className="field-label required">Email</span>
          <input value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} type="email" placeholder="nimal@example.com" required />
        </label>
        <label>
          <span className="field-label required">Username</span>
          <input value={form.username} onChange={(event) => setForm({ ...form, username: event.target.value })} placeholder="nimal.perera" required />
        </label>
        <label>
          <span className="field-label required">Password</span>
          <input value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} type="password" placeholder="Minimum 8 characters" required />
        </label>
        <label>
          <span className="field-label required">Role</span>
          <select value={form.role} onChange={(event) => setForm({ ...form, role: event.target.value as Role })}>
            <option value="DONOR">Donor</option>
            <option value="SHELTER_MANAGER">Shelter Manager</option>
            <option value="ADMIN">Admin</option>
          </select>
        </label>

        {form.role === "SHELTER_MANAGER" ? (
          <div className="shelter-subform">
            <h3 style={{ marginTop: "1rem", color: "#2563EB" }}>Shelter Details</h3>
            <label>
              <span className="field-label required">Shelter Name</span>
              <input value={form.shelterName} onChange={(event) => setForm({ ...form, shelterName: event.target.value })} placeholder="Colombo Central Shelter" required />
            </label>
            <label>
              <span className="field-label required">District</span>
              <input value={form.shelterDistrict} onChange={(event) => setForm({ ...form, shelterDistrict: event.target.value })} placeholder="Colombo" required />
            </label>
            <label>
              <span className="field-label required">Address Line 1</span>
              <input value={form.shelterAddressLine1} onChange={(event) => setForm({ ...form, shelterAddressLine1: event.target.value })} placeholder="123 Galle Road" required />
            </label>
            <label>
              <span className="field-label">Address Line 2</span>
              <input value={form.shelterAddressLine2} onChange={(event) => setForm({ ...form, shelterAddressLine2: event.target.value })} placeholder="Suite 4" />
            </label>
            <label>
              <span className="field-label required">Contact Name</span>
              <input value={form.shelterContactName} onChange={(event) => setForm({ ...form, shelterContactName: event.target.value })} placeholder="Saman Perera" required />
            </label>
            <label>
              <span className="field-label required">Contact Phone</span>
              <input value={form.shelterContactPhone} onChange={(event) => setForm({ ...form, shelterContactPhone: event.target.value })} placeholder="0771234567" required />
            </label>
          </div>
        ) : null}

        <p className="muted">
          All registrations (Admin, Shelter Manager, and Donor) require admin approval before login.
        </p>
        {success ? <p className="success-text">{success}</p> : null}
        {error ? <p className="error-text">{error}</p> : null}
        <button className="primary-button" type="submit">
          Create account
        </button>
        <p className="muted">
          Already registered? <Link to="/login">Back to login</Link>
        </p>
      </form>
    </div>
  );
}
