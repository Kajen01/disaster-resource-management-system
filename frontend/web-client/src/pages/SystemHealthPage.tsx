import { useEffect, useState } from "react";
import SectionCard from "../components/SectionCard";
import api from "../lib/api";
import type { ServiceHealth } from "../lib/types";

export default function SystemHealthPage() {
  const [health, setHealth] = useState<ServiceHealth[]>([]);

  async function loadHealth() {
    const { data } = await api.get<ServiceHealth[]>("/api/admin/health");
    setHealth(data);
  }

  useEffect(() => {
    void loadHealth();
  }, []);

  return (
    <div className="page-grid single">
      <SectionCard title="System Health">
        <div className="table-like">
          {health.map((service) => (
            <div className="table-row" key={service.serviceName}>
              <div>
                <strong>{service.serviceName}</strong>
                <p className="muted">{service.reachable ? "Reachable" : "Unreachable"}</p>
              </div>
              <div>
                <p>{service.status}</p>
              </div>
            </div>
          ))}
        </div>
      </SectionCard>
    </div>
  );
}
