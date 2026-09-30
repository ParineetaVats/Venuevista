import { Link } from 'react-router-dom'
import PageHeader from '../components/PageHeader.jsx'
export default function About() {
  return (
    <>
      <PageHeader color="#5f7f6a" title="About VenueVista" sub="Booking a hall used to mean weeks of calls. Now it takes a few clicks." />
      <section className="page">
        <div className="grid">
          <article className="card"><h3>Our mission</h3><p>Make every venue easy to find, compare and book, with prices shown up front.</p></article>
          <article className="card"><h3>What we offer</h3><p>Verified venues, live availability and one place to manage all your bookings.</p></article>
          <article className="card"><h3>Who it's for</h3><p>Families planning weddings, teams hosting offsites and friends throwing parties.</p></article>
        </div>
        <div className="stats"><div><b>250+</b><span>Verified venues</span></div><div><b>12,000</b><span>Events hosted</span></div><div><b>4.7/5</b><span>Average rating</span></div></div>
        <Link to="/contact" className="btn primary">Talk to our team</Link>
      </section>
    </>
  )
}
