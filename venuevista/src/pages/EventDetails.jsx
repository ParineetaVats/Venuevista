import { Link, useParams } from 'react-router-dom'
import { Calendar, Clock, MapPin, User, Armchair, Check } from 'lucide-react'
import { eventById, events, fmtDate } from '../data/events.js'
import { venueById, inr } from '../data.js'
import EventImage from '../components/EventImage.jsx'
import EventCard from '../components/EventCard.jsx'
import VenueImage from '../components/VenueImage.jsx'
import NotFound from './NotFound.jsx'
const rules = ['Carry a valid photo ID and your e-ticket.', 'Entry closes 30 minutes after start time.', 'Outside food, beverages and professional cameras are not allowed.']
const facilities = ['Parking', 'Wheelchair access', 'Food & beverages', 'Restrooms', 'Security screening', 'Free Wi-Fi']
export default function EventDetails() {
  const { id } = useParams()
  const e = eventById(id)
  if (!e) return <NotFound />
  const v = venueById(e.venue)
  const schedule = [['Doors open', e.time], ['Main programme', 'Starts shortly after'], ['Closing & exit', 'Approx. 3 hours later']]
  const related = events.filter((x) => x.id !== e.id && x.category === e.category).concat(events.filter((x) => x.id !== e.id && x.category !== e.category)).slice(0, 3)
  return (
    <>
      <div className="ev-banner"><EventImage category={e.category} height={300} label={e.title} />
        <div className="ev-banner-in"><span className="ev-cat">{e.category}</span><h1>{e.title}</h1></div></div>
      <section className="page ev-detail">
        <div>
          <ul className="ev-facts">
            <li><Calendar />{fmtDate(e.date)}</li><li><Clock />{e.time}</li><li><MapPin />{v.name}, {v.city}</li><li><User />{e.organizer}</li><li><Armchair />{e.left} of {e.seats} seats available</li>
          </ul>
          <h2>About this event</h2><p>{e.desc}</p>
          <h2>Event schedule</h2>
          <ul className="ev-sched">{schedule.map(([a, b]) => <li key={a}><b>{a}</b><span>{b}</span></li>)}</ul>
          <h2>Event rules</h2><ul className="ev-list">{rules.map((r) => <li key={r}><Check size={16} />{r}</li>)}</ul>
          <h2>Facilities</h2><div className="chips">{facilities.map((f) => <span className="tag" key={f}>{f}</span>)}</div>
          <h2>Refund policy</h2><p>Full refund up to 7 days before the event, 50% refund up to 48 hours before. No refunds after that. Cancel anytime from My Bookings.</p>
        </div>
        <aside className="card ev-buy">
          <div className="body">
            <p className="meta">Tickets from</p><b className="ev-price">{inr(e.price)}</b>
            <Link className="btn primary" to={`/events/${e.id}/seats`}>Choose Seats</Link>
            <Link className="btn" to={`/events/${e.id}/seats`}>Book Now</Link>
            <hr /><h3>Venue</h3>
          </div>
          <VenueImage v={v} />
          <div className="body"><b>{v.name}</b><span>{v.city} · Capacity {v.capacity}</span><Link className="more" to="/venues">Browse venues →</Link></div>
        </aside>
      </section>
      <section className="page"><h2>You may also like</h2><div className="vv-grid">{related.map((x) => <EventCard key={x.id} e={x} />)}</div></section>
    </>
  )
}
