import { createContext, useContext } from "react";
import type { AuthResponse } from "../lib/types";

type AuthContextValue = {
  auth: AuthResponse | null;
  setAuth: (auth: AuthResponse | null) => void;
};

export const AuthContext = createContext<AuthContextValue>({
  auth: null,
  setAuth: () => undefined
});

export function useAuth() {
  return useContext(AuthContext);
}
