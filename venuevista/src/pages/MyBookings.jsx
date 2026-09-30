import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Search, Ticket } from 'lucide-react'
import { getBookings, saveBookings, displayStatus } from '../data/bookings.js'
import { fmtDate } from '../data/events.js'
import { inr } from '../data.js'
import PageHeader from '../components/PageHeader.jsx'
import Modal from '../components/Modal.jsx'
export default function MyBookings() {
  const [items, setItems] = useState(getBookings), [q, setQ] = useState(''), [st, setSt] = useState('All'), [target, setTarget] = useState(null)
  const list = items.filter((b) => (st === 'All' || displayStatus(b) === st) && (b.id + b.title + b.venueName).toLowerCase().includes(q.trim().toLowerCase()))
  const cancel = () => { const n = items.map((b) => b.id === target.id ? { ...b, status: 'Cancelled', payment: 'Refunded' } : b); saveBookings(n); setItems(n); setTarget(null) }
  return (
    <>
      <PageHeader color="#1f3b73" title="My bookings" sub="Everything you've booked, in one place." />
      <section className="page narrow">
        <div className="ev-filters two-f"><label className="ev-search"><Search size={18} /><input aria-label="Search bookings" placeholder="Search by booking ID or event" value={q} onChange={(e) => setQ(e.target.value)} /></label>
          <select aria-label="Status" value={st} onChange={(e) => setSt(e.target.value)}>{['All', 'Confirmed', 'Pending', 'Cancelled', 'Completed'].map((s) => <option key={s}>{s}</option>)}</select></div>
        {!list.length ? <div className="empty"><Ticket size={40} /><p>{items.length ? 'No bookings match your filters.' : 'You have no bookings yet.'}</p><Link to="/events" className="btn primary">Browse events</Link></div> : (
          <div className="list">{list.map((b) => { const s = displayStatus(b); return (
            <article key={b.id} className="card body bk">
              <div><span className={`badge st-${s}`}>{s}</span> <span className="badge">{b.payment}</span><h3>{b.title}</h3>
                <p>{b.id} · {fmtDate(b.date)} · {b.venueName}<br />Seats: {b.seats.map((x) => x.id).join(', ')} · {inr(b.amounts.total)}</p></div>
              <div className="ev-btns"><Link className="btn sm" to={`/booking-confirmation/${b.id}`}>View Ticket</Link>{s === 'Confirmed' && <button className="btn sm" onClick={() => setTarget(b)}>Cancel Booking</button>}</div>
            </article>) })}</div>)}
      </section>
      <Modal open={!!target} title="Cancel this booking?" onClose={() => setTarget(null)}>
        <p>{target?.title} · {target?.id}. Your seats will be released and the amount refunded.</p>
        <div className="ev-btns"><button className="btn primary" onClick={cancel}>Yes, cancel</button><button className="btn" onClick={() => setTarget(null)}>Keep booking</button></div>
      </Modal>
    </>
  )
}
