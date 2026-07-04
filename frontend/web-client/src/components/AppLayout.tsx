import { Link, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../app/AuthContext";

export default function AppLayout() {
  const { auth, setAuth } = useAuth();
  const location = useLocation();

  const navItems = auth?.role === "ADMIN"
    ? [
        { to: "/", label: "Dashboard" },
        { to: "/users", label: "Users & Shelters" },
        { to: "/log-donation", label: "Donations" },
        { to: "/sharing", label: "Shortages" }
      ]
    : auth?.role === "SHELTER_MANAGER"
      ? [
          { to: "/", label: "Dashboard" },
          { to: "/inventory", label: "Inventory" },
          { to: "/sharing", label: "Transfers" }
        ]
      : [
          { to: "/", label: "Dashboard" },
          { to: "/inventory", label: "My Donations" }
        ];

  return (
    <div className="shell">
      <aside className="sidebar">
        <div>
          <p className="eyebrow">Disaster Resource Platform</p>
          <h1>DRMS Control</h1>
          <p className="muted">Distributed shelter operations and transparency console.</p>
        </div>
        <nav className="nav">
          {navItems.map((item) => (
            <Link
              key={item.to}
              className={location.pathname === item.to ? "nav-link active" : "nav-link"}
              to={item.to}
            >
              {item.label}
            </Link>
          ))}
        </nav>
        <div className="profile-card">
          <div>
            <strong>{auth?.username ?? "Guest"}</strong>
            <p className="muted">{auth?.fullName ?? "No session"}</p>
            <p className="muted">{auth?.role ?? ""}</p>
          </div>
          {auth ? (
            <button className="secondary-button" onClick={() => setAuth(null)}>
              Sign out
            </button>
          ) : null}
        </div>
      </aside>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
