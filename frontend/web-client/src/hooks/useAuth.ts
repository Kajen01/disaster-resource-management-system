import { useEffect, useState } from "react";
import type { AuthResponse } from "../lib/types";
import { loadAuth, saveAuth } from "../lib/storage";

export function useAuthState() {
  const [auth, setAuth] = useState<AuthResponse | null>(() => loadAuth());

  useEffect(() => {
    saveAuth(auth);
  }, [auth]);

  return { auth, setAuth };
}
