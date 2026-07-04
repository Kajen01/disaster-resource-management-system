import { ReactElement } from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../app/AuthContext";
import type { Role } from "../lib/types";

export default function RoleGuard({ children, roles }: { children: ReactElement; roles: Role[] }) {
  const { auth } = useAuth();
  if (!auth) {
    return <Navigate to="/login" replace />;
  }
  if (!roles.includes(auth.role)) {
    return <Navigate to="/" replace />;
  }
  return children;
}
