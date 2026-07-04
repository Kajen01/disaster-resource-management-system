import { FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../lib/api";
import { getApiErrorMessage } from "../lib/apiError";
import { useAuth } from "../app/AuthContext";
import type { AuthResponse } from "../lib/types";

export default function LoginPage() {
  const navigate = useNavigate();
  const { setAuth } = useAuth();
  const [identifier, setIdentifier] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setError("");
    try {
      const { data } = await api.post<AuthResponse>("/api/auth/login", { identifier, password });
      setAuth(data);
      navigate("/");
    } catch (err) {
      setError(getApiErrorMessage(err, "Login failed. Check the user account and password."));
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-form" onSubmit={onSubmit}>
        <p className="eyebrow">Secure Access</p>
        <h1>Sign in to DRMS</h1>
        <label>
          <span className="field-label required">Email or username</span>
          <input value={identifier} onChange={(event) => setIdentifier(event.target.value)} placeholder="admin01 or admin01@gmail.com" required />
        </label>
        <label>
          <span className="field-label required">Password</span>
          <input value={password} onChange={(event) => setPassword(event.target.value)} type="password" placeholder="Enter your account password" required />
        </label>
        {error ? <p className="error-text">{error}</p> : null}
        <button className="primary-button" type="submit">
          Sign in
        </button>
        <p className="muted">
          Need an account? <Link to="/register">Register here</Link>
        </p>
      </form>
    </div>
  );
}
