import type { AuthResponse } from "./types";

const AUTH_KEY = "drms-auth";

export function loadAuth(): AuthResponse | null {
  const raw = localStorage.getItem(AUTH_KEY);
  return raw ? (JSON.parse(raw) as AuthResponse) : null;
}

export function saveAuth(auth: AuthResponse | null) {
  if (!auth) {
    localStorage.removeItem(AUTH_KEY);
    return;
  }
  localStorage.setItem(AUTH_KEY, JSON.stringify(auth));
}
