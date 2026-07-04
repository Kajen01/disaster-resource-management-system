import { PropsWithChildren } from "react";

export default function SectionCard({ children, title }: PropsWithChildren<{ title: string }>) {
  return (
    <section className="panel">
      <div className="panel-header">
        <h2>{title}</h2>
      </div>
      {children}
    </section>
  );
}
