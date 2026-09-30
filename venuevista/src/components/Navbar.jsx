import { useState } from 'react'
import { Link, NavLink } from 'react-router-dom'
import { Search, Menu, X } from 'lucide-react'
import { useAuth } from '../auth.jsx'
import { Brand } from './Logo.jsx'
const links = [['/', 'Home'], ['/events', 'Events'], ['/venues', 'Venues'], ['/my-bookings', 'My Bookings'], ['/my-tickets', 'My Tickets'], ['/about', 'About']]
export default function Navbar() {
  const [open, setOpen] = useState(false)
  const { user, logout } = useAuth()
  const close = () => setOpen(false)
  return (
    <header className="nav">
      <Link to="/" className="logo" onClick={close} aria-label="VenueVista home"><Brand tagline /></Link>
      <button className="burger" aria-label="Toggle menu" aria-expanded={open} onClick={() => setOpen(!open)}>{open ? <X /> : <Menu />}</button>
      <nav className={open ? 'links open' : 'links'}>
        {links.map(([to, label]) => (
          <NavLink key={to} to={to} end={to === '/'} onClick={close} className={({ isActive }) => (isActive ? 'active' : '')}>{label}</NavLink>
        ))}
        <Link to="/events" onClick={close} className="nav-search" aria-label="Search events"><Search size={18} /></Link>
        {user ? (
          <>
            <span className="hi">Hi, {user.name.split(' ')[0]}</span>
            <button className="btn login" onClick={() => { logout(); close() }}>Logout</button>
          </>
        ) : (
          <NavLink to="/login" onClick={close} className="btn outline">Login</NavLink>
        )}
        <Link to="/events" onClick={close} className="btn login">Book Now</Link>
      </nav>
    </header>
  )
}
