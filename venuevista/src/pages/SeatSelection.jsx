import { useMemo, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { eventById, fmtDate } from '../data/events.js'
import { venueById, inr } from '../data.js'
import { getLayout, bookedSeats, seatPrice, fees, types } from '../data/seating.js'
import { setDraft } from '../data/bookings.js'
import SeatingChart, { Legend } from '../components/SeatingChart.jsx'
import NotFound from './NotFound.jsx'
function Seats({ e }) {
  const nav = useNavigate(), v = venueById(e.venue)
  const layout = useMemo(() => getLayout(e.venue), [e.venue])
  const booked = useMemo(() => bookedSeats(e), [e])
  const typeOf = useMemo(() => Object.fromEntries(layout.rows.flatMap((r) => r.seats.map((s) => [r.id + s.n, s.type]))), [layout])
  const [sel, setSel] = useState([]), [msg, setMsg] = useState('')
  const toggle = (r, s, id) => {
    setMsg('')
    if (sel.includes(id)) return setSel(sel.filter((x) => x !== id))
    if (sel.length >= 8) return setMsg('You can select up to 8 seats per booking.')
    setSel([...sel, id])
  }
  const seats = sel.map((id) => ({ id, type: typeOf[id], price: seatPrice(e, typeOf[id]) }))
  const f = fees(seats.reduce((n, s) => n + s.price, 0), seats.length)
  const prices = Object.fromEntries(types.map((t) => [t, inr(seatPrice(e, t))]))
  const go = () => { setDraft({ eventId: e.id, seats }); nav('/checkout') }
  return (
    <section className="page">
      <p className="meta"><Link to={`/events/${e.id}`}>← {e.title}</Link></p>
      <h1>Select your seats</h1>
      <p>{fmtDate(e.date)} · {e.time} · {v.name}, {v.city}</p>
      <div className="sc-wrap">
        <div className="card body"><SeatingChart layout={layout} booked={booked} selected={new Set(sel)} onSeat={toggle} /><Legend prices={prices} /></div>
        <aside className="card body sc-sum">
          <h3>Booking summary</h3>
          {seats.length === 0 ? <p className="meta">Click seats on the chart to select them.</p> : (
            <ul className="sum-list">{seats.map((s) => <li key={s.id}><span>{s.id} · {s.type}</span><b>{inr(s.price)}</b></li>)}</ul>
          )}
          <div className="sum-row"><span>Ticket quantity</span><b>{seats.length}</b></div>
          <div className="sum-row"><span>Ticket price</span><b>{inr(f.sub)}</b></div>
          <div className="sum-row"><span>Convenience fee</span><b>{inr(f.fee)}</b></div>
          <div className="sum-row total"><span>Total amount</span><b>{inr(f.sub + f.fee)}</b></div>
          <small className="meta">Taxes are added at checkout.</small>
          {msg && <p className="err" role="alert">{msg}</p>}
          <button className="btn primary" disabled={!seats.length} onClick={go}>Proceed to Checkout</button>
        </aside>
      </div>
    </section>
  )
}
export default function SeatSelection() {
  const e = eventById(useParams().id)
  return e ? <Seats e={e} /> : <NotFound />
}
