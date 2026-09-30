import { Link } from 'react-router-dom'
import { Facebook, Instagram, Twitter, Linkedin } from 'lucide-react'
import { Brand } from './Logo.jsx'
const quick = [['/', 'Home'], ['/events', 'Events'], ['/venues', 'Venues'], ['/about', 'About'], ['/contact', 'Contact'], ['/my-bookings', 'My Bookings'], ['/my-tickets', 'My Tickets']]
const services = [['Event Ticketing', '/events'], ['Venue Management', '/venues'], ['Seating Management', '/admin/seating'], ['Ticket Sales', '/admin/ticket-sales'], ['Sales Analytics', '/admin/analytics']]
export default function Footer() {
  return (
    <footer className="vv-footer">
      <div className="vv-foot-grid">
        <div><Brand tagline light /><p>Discover events, explore venues and book your perfect seat with confidence.</p>
          <div className="social">{[Facebook, Instagram, Twitter, Linkedin].map((I, i) => <a key={i} href="#top" aria-label="Social link" onClick={(e) => e.preventDefault()}><I size={18} /></a>)}</div></div>
        <div><h4>Quick Links</h4>{quick.map(([to, l]) => <Link key={l} to={to}>{l}</Link>)}</div>
        <div><h4>Services</h4>{services.map(([l, to]) => <Link key={l} to={to}>{l}</Link>)}</div>
      </div>
      <p className="copy">© 2026 VenueVista. All rights reserved.</p>
    </footer>
  )
}
