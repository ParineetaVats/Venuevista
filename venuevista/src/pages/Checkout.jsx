import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { CreditCard, Smartphone, Landmark } from 'lucide-react'
import { useAuth } from '../auth.jsx'
import { getDraft, clearDraft, addBooking } from '../data/bookings.js'
import { eventById, fmtDate } from '../data/events.js'
import { venueById, inr } from '../data.js'
import { fees, bookedSeats } from '../data/seating.js'
import { Brand } from '../components/Logo.jsx'
const methods = [['card', 'Card', CreditCard], ['upi', 'UPI', Smartphone], ['bank', 'Net Banking', Landmark]]
export default function Checkout() {
  const draft = getDraft(), { user } = useAuth(), nav = useNavigate()
  const [f, setF] = useState({ name: user?.name || '', email: user?.email || '', phone: '', card: '', exp: '', cvv: '', upi: '', bank: '' })
  const [m, setM] = useState('card'), [err, setErr] = useState({}), [busy, setBusy] = useState(false)
  const e = draft && eventById(draft.eventId)
  if (!e || !draft.seats?.length) return <Navigate to="/events" replace />
  const v = venueById(e.venue), amt = fees(draft.seats.reduce((n, s) => n + s.price, 0), draft.seats.length)
  const set = (k) => (ev) => setF({ ...f, [k]: ev.target.value })
  const validate = () => {
    const x = {}
    if (f.name.trim().length < 2) x.name = 'Enter your full name'
    if (!/^\S+@\S+\.\S+$/.test(f.email)) x.email = 'Enter a valid email'
    if (!/^\d{10}$/.test(f.phone)) x.phone = 'Enter a 10-digit phone number'
    if (m === 'card') { if (!/^\d{16}$/.test(f.card.replace(/\s/g, ''))) x.card = 'Enter a 16-digit card number'; if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(f.exp)) x.exp = 'Use MM/YY'; if (!/^\d{3}$/.test(f.cvv)) x.cvv = '3 digits' }
    if (m === 'upi' && !/^[\w.-]+@[\w]+$/.test(f.upi)) x.upi = 'Enter a valid UPI ID (name@bank)'
    if (m === 'bank' && !f.bank) x.bank = 'Select a bank'
    return x
  }
  const submit = (ev) => {
    ev.preventDefault()
    const x = validate(); setErr(x)
    if (Object.keys(x).length) return
    const taken = bookedSeats(e)
    if (draft.seats.some((s) => taken.has(s.id))) return setErr({ form: 'Sorry, some of your seats were just booked. Please choose again.' })
    setBusy(true)
    setTimeout(() => {
      const id = 'VV' + Date.now().toString(36).toUpperCase()
      addBooking({ id, eventId: e.id, title: e.title, category: e.category, date: e.date, time: e.time, venueName: v.name, city: v.city, seats: draft.seats, qty: draft.seats.length, amounts: amt, customer: { name: f.name, email: f.email, phone: f.phone }, method: m, status: 'Confirmed', payment: 'Paid', createdAt: new Date().toISOString() })
      clearDraft(); nav(`/booking-confirmation/${id}`)
    }, 900)
  }
  const field = (k, label, ph, extra = {}) => <label>{label}<input value={f[k]} onChange={set(k)} placeholder={ph} aria-invalid={!!err[k]} {...extra} />{err[k] && <span className="err">{err[k]}</span>}</label>
  return (
    <section className="page">
      <Brand tagline /><h1>Checkout</h1>
      <form className="co" onSubmit={submit} noValidate>
        <div className="form">
          <h3>Customer information</h3>
          {field('name', 'Full name', 'Aarav Sharma')}
          <div className="two">{field('email', 'Email', 'you@example.com', { type: 'email' })}{field('phone', 'Phone', '10-digit mobile', { inputMode: 'numeric', maxLength: 10 })}</div>
          <h3>Payment method</h3>
          <div className="chips">{methods.map(([k, l, I]) => <button type="button" key={k} className={m === k ? 'chip on' : 'chip'} onClick={() => setM(k)}><I size={14} /> {l}</button>)}</div>
          {m === 'card' && <>{field('card', 'Card number', '1234 5678 9012 3456', { inputMode: 'numeric' })}<div className="two">{field('exp', 'Expiry', 'MM/YY')}{field('cvv', 'CVV', '123', { type: 'password', maxLength: 3 })}</div></>}
          {m === 'upi' && field('upi', 'UPI ID', 'name@bank')}
          {m === 'bank' && <label>Bank<select value={f.bank} onChange={set('bank')}><option value="">Select bank</option>{['HDFC Bank', 'SBI', 'ICICI Bank', 'Axis Bank'].map((b) => <option key={b}>{b}</option>)}</select>{err.bank && <span className="err">{err.bank}</span>}</label>}
          <small className="meta">Demo payment — no real charge is made.</small>
          {err.form && <p className="err" role="alert">{err.form}</p>}
          <button className="btn primary" disabled={busy}>{busy ? 'Processing…' : `Confirm Booking · ${inr(amt.total)}`}</button>
        </div>
        <aside className="card body sc-sum">
          <h3>{e.title}</h3><p className="meta">{fmtDate(e.date)} · {e.time}<br />{v.name}, {v.city}</p>
          <ul className="sum-list">{draft.seats.map((s) => <li key={s.id}><span>{s.id} · {s.type}</span><b>{inr(s.price)}</b></li>)}</ul>
          <div className="sum-row"><span>Subtotal</span><b>{inr(amt.sub)}</b></div>
          <div className="sum-row"><span>Convenience fee</span><b>{inr(amt.fee)}</b></div>
          <div className="sum-row"><span>Taxes (5%)</span><b>{inr(amt.tax)}</b></div>
          <div className="sum-row total"><span>Total</span><b>{inr(amt.total)}</b></div>
        </aside>
      </form>
    </section>
  )
}
