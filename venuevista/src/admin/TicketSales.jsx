import { useState } from 'react'
import { Ticket, CalendarDays, CalendarRange, CalendarClock, IndianRupee } from 'lucide-react'
import { events, fmtDate } from '../data/events.js'
import { venues, venueById, inr } from '../data.js'
import { getBookings } from '../data/bookings.js'
import { eventSales } from '../data/seating.js'
import { StatCard } from '../components/ChartCard.jsx'
export default function TicketSales() {
  const [f, setF] = useState({ event: 'All', venue: 'All', status: 'All', from: '' })
  const bk = getBookings().filter((b) => b.status !== 'Cancelled'), now = Date.now(), ago = (d) => bk.filter((b) => now - new Date(b.createdAt) < d * 864e5).reduce((n, b) => n + b.qty, 0)
  const rows = events.map((e) => ({ e, v: venueById(e.venue), ...eventSales(e, getBookings()) }))
  const total = rows.reduce((n, r) => n + r.sold, 0), rev = rows.reduce((n, r) => n + r.revenue, 0)
  const list = rows.filter((r) => (f.event === 'All' || r.e.id === +f.event) && (f.venue === 'All' || r.v.id === +f.venue) && (f.status === 'All' || r.status === f.status) && (!f.from || r.e.date >= f.from))
  const set = (k) => (ev) => setF({ ...f, [k]: ev.target.value })
  return (
    <>
      <h1>Ticket sales</h1>
      <div className="vv-grid four">
        <StatCard icon={Ticket} label="Total tickets sold" value={total.toLocaleString('en-IN')} />
        <StatCard icon={CalendarClock} label="Today's sales" value={38 + ago(1)} note="tickets" />
        <StatCard icon={CalendarDays} label="This week" value={264 + ago(7)} note="tickets" />
        <StatCard icon={CalendarRange} label="This month" value={(1047 + ago(30)).toLocaleString('en-IN')} note="tickets" />
        <StatCard icon={IndianRupee} label="Revenue" value={inr(rev)} />
      </div>
      <div className="ev-filters ad-f">
        <select aria-label="Event" value={f.event} onChange={set('event')}><option value="All">All events</option>{events.map((e) => <option key={e.id} value={e.id}>{e.title}</option>)}</select>
        <select aria-label="Venue" value={f.venue} onChange={set('venue')}><option value="All">All venues</option>{venues.map((v) => <option key={v.id} value={v.id}>{v.name}</option>)}</select>
        <select aria-label="Status" value={f.status} onChange={set('status')}>{['All', 'On Sale', 'Selling Fast', 'Sold Out'].map((s) => <option key={s}>{s}</option>)}</select>
        <input type="date" aria-label="From date" value={f.from} onChange={set('from')} />
      </div>
      <div className="tbl"><table>
        <thead><tr>{['Event', 'Venue', 'Date', 'Tickets sold', 'Available', 'Revenue', 'Status'].map((h) => <th key={h}>{h}</th>)}</tr></thead>
        <tbody>{list.map(({ e, v, sold, left, revenue, status }) => <tr key={e.id}><td><b>{e.title}</b></td><td>{v.name}</td><td>{fmtDate(e.date)}</td><td>{sold}</td><td>{left}</td><td>{inr(revenue)}</td><td><span className={`badge s-${status.replace(' ', '')}`}>{status}</span></td></tr>)}
          {!list.length && <tr><td colSpan="7" className="empty">No sales match these filters.</td></tr>}</tbody></table></div>
    </>
  )
}
