import { NavLink, Outlet, Link } from 'react-router-dom'
import { Ticket, BarChart3, Armchair, Home } from 'lucide-react'
import { Brand } from '../components/Logo.jsx'
const items = [['/admin/ticket-sales', 'Ticket Sales', Ticket], ['/admin/analytics', 'Analytics', BarChart3], ['/admin/seating', 'Seating', Armchair]]
export default function AdminLayout() {
  return (
    <div className="ad">
      <aside className="ad-side">
        <Link to="/" aria-label="VenueVista home"><Brand tagline light /></Link>
        <nav aria-label="Admin">{items.map(([to, l, I]) => <NavLink key={to} to={to} className={({ isActive }) => (isActive ? 'on' : '')}><I size={18} />{l}</NavLink>)}
          <Link to="/"><Home size={18} />Back to site</Link></nav>
      </aside>
      <main className="ad-main"><Outlet /></main>
    </div>
  )
}
