import { useEffect } from 'react'
import { Link, useParams, useSearchParams } from 'react-router-dom'
import { CheckCircle2 } from 'lucide-react'
import { getBooking } from '../data/bookings.js'
import { fmtDate } from '../data/events.js'
import { inr } from '../data.js'
import { Brand } from '../components/Logo.jsx'
import NotFound from './NotFound.jsx'
export default function Confirmation() {
  const b = getBooking(useParams().id), [sp] = useSearchParams()
  useEffect(() => { if (b && sp.get('print')) { const t = setTimeout(() => window.print(), 400); return () => clearTimeout(t) } }, [b, sp])
  if (!b) return <NotFound />
  const rows = [['Event', b.title], ['Date', fmtDate(b.date)], ['Time', b.time], ['Venue', `${b.venueName}, ${b.city}`], ['Seats', b.seats.map((s) => s.id).join(', ')], ['Tickets', b.qty], ['Amount paid', inr(b.amounts.total)], ['Customer', b.customer.name], ['Booked on', new Date(b.createdAt).toLocaleDateString('en-IN')]]
  return (
    <section className="page narrow">
      <div className="ok no-print"><CheckCircle2 size={44} /><h1>Booking Confirmed</h1><p>A confirmation has been saved to My Bookings.</p></div>
      <article className="card ticket">
        <div className="ticket-head"><Brand tagline light /><b>{b.id}</b></div>
        <dl>{rows.map(([k, val]) => <div key={k}><dt>{k}</dt><dd>{val}</dd></div>)}</dl>
      </article>
      <div className="vv-cta no-print"><Link className="btn primary" to="/my-bookings">View My Booking</Link><button className="btn" onClick={() => window.print()}>Download Ticket</button><Link className="btn" to="/events">Back to Events</Link></div>
    </section>
  )
}
