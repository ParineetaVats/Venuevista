import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Ticket } from 'lucide-react'
import { getBookings, displayStatus } from '../data/bookings.js'
import { fmtDate } from '../data/events.js'
import PageHeader from '../components/PageHeader.jsx'
const Qr = ({ seed }) => {
  let h = 7; for (const c of seed) h = (h * 33 + c.charCodeAt(0)) >>> 0
  return <svg viewBox="0 0 9 9" className="qr" role="img" aria-label="QR code">{Array.from({ length: 81 }, (_, i) => { h = (h * 1103515245 + 12345) >>> 0; return (h >> 16) & 1 || [0, 1, 7, 8, 9, 17, 63, 71, 72, 73, 79, 80].includes(i) ? <rect key={i} x={i % 9} y={Math.floor(i / 9)} width="1" height="1" /> : null })}</svg>
}
export default function MyTickets() {
  const [note, setNote] = useState('')
  const tickets = getBookings().filter((b) => b.status !== 'Cancelled').flatMap((b) => b.seats.map((s) => ({ b, s })))
  const share = async (b) => {
    const url = `${location.origin}/booking-confirmation/${b.id}`
    try { if (navigator.share) await navigator.share({ title: b.title, url }); else { await navigator.clipboard.writeText(url); setNote('Ticket link copied!') } } catch { setNote('Could not share this ticket.') }
  }
  return (
    <>
      <PageHeader color="#0f6b4f" title="My tickets" sub="Your digital tickets, ready to scan at the gate." />
      <section className="page">
        {note && <p className="notice" role="status">{note}</p>}
        {!tickets.length ? <div className="empty"><Ticket size={40} /><p>No tickets yet.</p><Link to="/events" className="btn primary">Browse events</Link></div> : (
          <div className="vv-grid">{tickets.map(({ b, s }) => (
            <article key={b.id + s.id} className="card tk"><div className="body"><span className="badge st-Confirmed">{displayStatus(b)}</span><h3>{b.title}</h3>
              <p>{fmtDate(b.date)} · {b.time}<br />{b.venueName}<br />Seat <b>{s.id}</b> ({s.type}) · {b.id}</p>
              <div className="ev-btns"><Link className="btn sm" to={`/booking-confirmation/${b.id}`}>View Ticket</Link><Link className="btn sm" to={`/booking-confirmation/${b.id}?print=1`}>Download</Link><button className="btn sm" onClick={() => share(b)}>Share</button></div></div>
              <Qr seed={b.id + s.id} /></article>))}</div>)}
      </section>
    </>
  )
}
