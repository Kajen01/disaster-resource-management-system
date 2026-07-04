import { Navigate, Route, Routes } from "react-router-dom";
import { AuthContext } from "./AuthContext";
import { useAuthState } from "../hooks/useAuth";
import AppLayout from "../components/AppLayout";
import AuthGuard from "../components/AuthGuard";
import LoginPage from "../pages/LoginPage";
import RegisterPage from "../pages/RegisterPage";
import OverviewPage from "../pages/OverviewPage";
import LogDonationPage from "../pages/LogDonationPage";
import InventoryPage from "../pages/InventoryPage";
import SharingPage from "../pages/SharingPage";
import UsersPage from "../pages/UsersPage";
import RoleGuard from "../components/RoleGuard";

export default function App() {
  const authState = useAuthState();

  return (
    <AuthContext.Provider value={authState}>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route
          path="/"
          element={
            <AuthGuard>
              <AppLayout />
            </AuthGuard>
          }
        >
          <Route index element={<OverviewPage />} />
          <Route path="inventory" element={<InventoryPage />} />
          <Route path="sharing" element={<SharingPage />} />
          <Route
            path="log-donation"
            element={
              <RoleGuard roles={["ADMIN"]}>
                <LogDonationPage />
              </RoleGuard>
            }
          />
          <Route
            path="users"
            element={
              <RoleGuard roles={["ADMIN"]}>
                <UsersPage />
              </RoleGuard>
            }
          />
        </Route>
        <Route path="*" element={<Navigate to={authState.auth ? "/" : "/login"} replace />} />
      </Routes>
    </AuthContext.Provider>
  );
}
