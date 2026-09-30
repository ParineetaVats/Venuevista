export function StatCard({ icon: I, label, value, note }) {
  return <div className="card body stat"><I className="vv-ico" /><span>{label}</span><b>{value}</b>{note && <small>{note}</small>}</div>
}
export function ChartCard({ title, sub, children, height = 260 }) {
  return <div className="card body chart"><h3>{title}</h3>{sub && <p className="meta">{sub}</p>}<div style={{ height }}>{children}</div></div>
}
