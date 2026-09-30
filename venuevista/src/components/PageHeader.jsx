export default function PageHeader({ title, sub, color }) {
  return <header className="phead" style={{ '--h': color }}><div><h1>{title}</h1><p>{sub}</p></div></header>
}
