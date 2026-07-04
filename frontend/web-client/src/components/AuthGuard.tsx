import { ReactElement } from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../app/AuthContext";

export default function AuthGuard({ children }: { children: ReactElement }) {
  const { auth } = useAuth();
  if (!auth) {
    return <Navigate to="/login" replace />;
  }
  return children;
}
