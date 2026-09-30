import { getBookings } from './bookings.js'
export const types = ['VIP', 'Premium', 'Standard']
export const mult = { VIP: 2, Premium: 1.5, Standard: 1 }
const spec = [['A', 'VIP', 8], ['B', 'VIP', 8], ['C', 'Premium', 10], ['D', 'Premium', 10], ['E', 'Standard', 12], ['F', 'Standard', 12]]
export const defaultLayout = () => ({ rows: spec.map(([id, type, n]) => ({ id, seats: Array.from({ length: n }, (_, i) => ({ n: i + 1, type, off: false })) })) })
export const getLayout = (vid) => { try { const s = JSON.parse(localStorage.getItem('vv-layout-' + vid)); if (s?.rows) return s } catch {} return defaultLayout() }
export const saveLayout = (vid, l) => { try { localStorage.setItem('vv-layout-' + vid, JSON.stringify(l)) } catch {} }
export const seatPrice = (e, type) => Math.round(e.price * mult[type])
const hash = (s) => { let h = 0; for (const c of s) h = (h * 31 + c.charCodeAt(0)) >>> 0; return h % 100 }
// Seats already sold: deterministic demo data (from the event's sold ratio) + real bookings made in this browser.
export const bookedSeats = (e) => {
  const pct = Math.round((1 - e.left / e.seats) * 100), set = new Set()
  getLayout(e.venue).rows.forEach((r) => r.seats.forEach((s) => { if (hash(e.id + '-' + r.id + s.n) < pct) set.add(r.id + s.n) }))
  getBookings().filter((b) => b.eventId === e.id && b.status !== 'Cancelled').forEach((b) => b.seats.forEach((s) => set.add(s.id)))
  return set
}
export const fees = (sub, qty) => { const fee = 40 * qty, tax = Math.round(sub * 0.05); return { sub, fee, tax, total: sub + fee + tax } }
export const eventSales = (e, bookings) => {
  const live = bookings.filter((b) => b.eventId === e.id && b.status !== 'Cancelled')
  const qty = live.reduce((n, b) => n + b.qty, 0), liveRev = live.reduce((n, b) => n + b.amounts.sub, 0)
  const left = Math.max(0, e.left - qty), sold = e.seats - left
  return { sold, left, revenue: (e.seats - e.left) * Math.round(e.price * 1.25) + liveRev, status: left === 0 ? 'Sold Out' : left / e.seats < 0.3 ? 'Selling Fast' : 'On Sale' }
}
export const monthly = [['Jan', 412000, 610], ['Feb', 465000, 702], ['Mar', 538000, 795], ['Apr', 497000, 741], ['May', 612000, 880], ['Jun', 701000, 1010], ['Jul', 655000, 950], ['Aug', 784000, 1120], ['Sep', 842000, 1245]].map(([m, revenue, tickets]) => ({ m, revenue, tickets }))
