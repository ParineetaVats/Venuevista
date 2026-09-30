import { useState } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { venues, inr, getBookings, saveBookings } from '../data.js'
import PageHeader from '../components/PageHeader.jsx'
import { useAuth } from '../auth.jsx'
import VenueImage from '../components/VenueImage.jsx'
export default function Venues() {
  const [sp] = useSearchParams()
  const { user } = useAuth()
  const nav = useNavigate()
  const [q, setQ] = useState('')
  const [type, setType] = useState(sp.get('type') || 'All')
  const [booked, setBooked] = useState(null)
  const list = venues.filter((v) => (type === 'All' || v.type === type) && (v.name + v.city).toLowerCase().includes(q.toLowerCase()))
  const book = (v) => { if (!user) return nav('/login', { state: { from: '/venues' } }); saveBookings([...getBookings(), { id: Date.now(), venue: v.name, vid: v.id, date: '15 Dec 2026', amount: v.price }]); setBooked(v.name) }
  return (
    <>
      <PageHeader color="#3f6078" title="Venues" sub="Search by name or city and filter by the kind of event you're planning." />
      <section className="page">
        <div className="filters">
          <input placeholder="Search by name or city" value={q} onChange={(e) => setQ(e.target.value)} aria-label="Search venues" />
          <select value={type} onChange={(e) => setType(e.target.value)} aria-label="Event type">{['All', 'Wedding', 'Corporate', 'Party'].map((x) => <option key={x}>{x}</option>)}</select>
        </div>
        {booked && <p className="notice" role="status">Booked {booked}. See it in My Bookings.</p>}
        <div className="grid">
          {list.map((v) => (
            <article className="card photo" key={v.id}><VenueImage v={v} /><div className="body"><span className="tag">{v.type}</span><h3>{v.name}</h3><p>{v.city} · up to {v.capacity} guests</p><strong>{inr(v.price)} / day</strong><button className="btn primary" onClick={() => book(v)}>Book this venue</button></div></article>
          ))}
        </div>
        {list.length === 0 && <p>No venues match. Try a different city or event type.</p>}
      </section>
    </>
  )
}
