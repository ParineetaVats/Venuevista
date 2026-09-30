import { Link } from 'react-router-dom'
import { Calendar, Clock, MapPin, Armchair } from 'lucide-react'
import EventImage from './EventImage.jsx'
import { venueById, inr } from '../data.js'
import { fmtDate } from '../data/events.js'
export default function EventCard({ e }) {
  const v = venueById(e.venue)
  return (
    <article className="card ev-card">
      <div className="ev-top"><EventImage category={e.category} /><span className="ev-cat">{e.category}</span></div>
      <div className="body">
        <h3>{e.title}</h3>
        <ul className="ev-meta">
          <li><Calendar size={15} />{fmtDate(e.date)}</li><li><Clock size={15} />{e.time}</li>
          <li><MapPin size={15} />{v.name}, {v.city}</li><li><Armchair size={15} />{e.left} seats left</li>
        </ul>
        <div className="ev-foot"><span>From <b>{inr(e.price)}</b></span>
          <span className="ev-btns"><Link className="btn sm" to={`/events/${e.id}`}>Details</Link><Link className="btn primary sm" to={`/events/${e.id}/seats`}>Book Now</Link></span></div>
      </div>
    </article>
  )
}
