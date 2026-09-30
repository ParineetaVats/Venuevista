import { Link } from 'react-router-dom'
import { Search, Ticket, Armchair, BarChart3, ShieldCheck, Star } from 'lucide-react'
import { events, categories } from '../data/events.js'
import { venues } from '../data.js'
import EventCard from '../components/EventCard.jsx'
import EventImage from '../components/EventImage.jsx'
import VenueImage from '../components/VenueImage.jsx'
const upcoming = [...events].sort((a, b) => a.date.localeCompare(b.date)).slice(0, 3)
const why = [[Ticket, 'Instant e-tickets', 'Book in a minute and get your digital ticket right away.'], [Armchair, 'Pick your exact seat', 'See the full seating chart and choose the view you want.'], [ShieldCheck, 'Safe & flexible', 'Transparent pricing and easy cancellations from My Bookings.'], [BarChart3, 'Built for organisers', 'Venue, seating and sales tools in one dashboard.']]
const steps = [['1', 'Discover', 'Browse events by category, city and date.'], ['2', 'Choose seats', 'Pick from VIP, Premium or Standard sections.'], ['3', 'Pay securely', 'Confirm your booking in a few taps.'], ['4', 'Enjoy the show', 'Show your e-ticket at the gate.']]
const stats = [['10K+', 'Tickets Sold'], ['250+', 'Events'], ['80+', 'Venues'], ['50K+', 'Happy Users']]
const reviews = [['Aarav M.', 'Choosing my exact seat made booking so easy. Loved the ticket page!'], ['Neha S.', 'Clean, fast and the venue details helped me plan the whole evening.'], ['Rohan K.', 'We run corporate events here — the dashboard is a real time saver.']]
export default function Home() {
  return (
    <>
      <section className="vv-hero">
        <div className="vv-hero-in">
          <div>
            <h1>Experience Events. Book Your Seat. Create Memories.</h1>
            <p>Discover events, explore venues, choose your perfect seat and book tickets effortlessly with VenueVista.</p>
            <div className="vv-cta"><Link className="btn login" to="/events">Explore Events</Link><Link className="btn ghost-light" to="/venues">Explore Venues</Link></div>
          </div>
          <div className="vv-hero-art"><EventImage category="Concerts" height={330} label="Live concert" /><div className="vv-float"><Search size={16} /> {events.length} events this season</div></div>
        </div>
      </section>
      <section className="page"><h2>Featured events</h2><div className="vv-grid">{events.filter((e) => e.featured).slice(0, 3).map((e) => <EventCard key={e.id} e={e} />)}</div></section>
      <section className="page"><h2>Event categories</h2>
        <div className="vv-cats">{categories.map((c) => <Link key={c} to={`/events?cat=${encodeURIComponent(c)}`} className="vv-cat"><EventImage category={c} height={90} />{c}</Link>)}</div></section>
      <section className="page"><h2>Upcoming events</h2><div className="vv-grid">{upcoming.map((e) => <EventCard key={e.id} e={e} />)}</div></section>
      <section className="page"><h2>Popular venues</h2>
        <div className="vv-grid">{venues.slice(0, 3).map((v) => <Link key={v.id} to="/venues" className="card vv-venue"><VenueImage v={v} /><div className="body"><h3>{v.name}</h3><p>{v.city} · Capacity {v.capacity}</p></div></Link>)}</div></section>
      <section className="page"><h2>Why choose VenueVista</h2>
        <div className="vv-grid four">{why.map(([I, t, d]) => <div className="card body" key={t}><I className="vv-ico" /><h3>{t}</h3><p>{d}</p></div>)}</div></section>
      <section className="page"><h2>How it works</h2>
        <div className="vv-grid four">{steps.map(([n, t, d]) => <div className="card body" key={n}><span className="vv-step">{n}</span><h3>{t}</h3><p>{d}</p></div>)}</div></section>
      <section className="vv-statband">{stats.map(([n, l]) => <div key={l}><b>{n}</b><span>{l}</span></div>)}</section>
      <section className="page"><h2>What our customers say</h2>
        <div className="vv-grid">{reviews.map(([n, t]) => <blockquote className="card body vv-quote" key={n}><div>{[1, 2, 3, 4, 5].map((i) => <Star key={i} size={16} fill="#c8a45c" stroke="#c8a45c" />)}</div><p>{t}</p><b>{n}</b></blockquote>)}</div></section>
      <section className="page"><div className="vv-final"><h2>Ready for your next unforgettable event?</h2><Link className="btn login" to="/events">Book Tickets Now</Link></div></section>
    </>
  )
}
