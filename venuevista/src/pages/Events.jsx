import { useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Search, SearchX } from 'lucide-react'
import { events, categories } from '../data/events.js'
import { venueById } from '../data.js'
import PageHeader from '../components/PageHeader.jsx'
import EventCard from '../components/EventCard.jsx'
const cities = [...new Set(events.map((e) => venueById(e.venue).city))]
export default function Events() {
  const [sp] = useSearchParams()
  const [q, setQ] = useState(sp.get('q') || '')
  const [cat, setCat] = useState(sp.get('cat') || 'All')
  const [city, setCity] = useState('All')
  const [from, setFrom] = useState('')
  const [sort, setSort] = useState('date')
  const list = useMemo(() => {
    const t = q.trim().toLowerCase()
    const r = events.filter((e) => (cat === 'All' || e.category === cat) && (city === 'All' || venueById(e.venue).city === city) && (!from || e.date >= from) && (!t || (e.title + e.category + venueById(e.venue).name).toLowerCase().includes(t)))
    return r.sort((a, b) => sort === 'price-asc' ? a.price - b.price : sort === 'price-desc' ? b.price - a.price : a.date.localeCompare(b.date))
  }, [q, cat, city, from, sort])
  const reset = () => { setQ(''); setCat('All'); setCity('All'); setFrom(''); setSort('date') }
  return (
    <>
      <PageHeader color="#5b2a86" title="Discover events" sub="Concerts, sports, comedy, theatre and more — find your next great night out." />
      <section className="page">
        <div className="ev-filters">
          <label className="ev-search"><Search size={18} /><input aria-label="Search events" placeholder="Search events, venues or categories" value={q} onChange={(e) => setQ(e.target.value)} /></label>
          <select aria-label="City" value={city} onChange={(e) => setCity(e.target.value)}><option value="All">All locations</option>{cities.map((c) => <option key={c}>{c}</option>)}</select>
          <input type="date" aria-label="From date" value={from} onChange={(e) => setFrom(e.target.value)} />
          <select aria-label="Sort" value={sort} onChange={(e) => setSort(e.target.value)}><option value="date">Soonest first</option><option value="price-asc">Price: low to high</option><option value="price-desc">Price: high to low</option></select>
        </div>
        <div className="chips">{['All', ...categories].map((x) => <button key={x} className={cat === x ? 'chip on' : 'chip'} onClick={() => setCat(x)}>{x}</button>)}</div>
        <p className="meta">{list.length} event{list.length !== 1 && 's'} found</p>
        {list.length ? <div className="vv-grid">{list.map((e) => <EventCard key={e.id} e={e} />)}</div>
          : <div className="empty"><SearchX size={40} /><p>No events match your filters.</p><button className="btn primary" onClick={reset}>Clear filters</button></div>}
      </section>
    </>
  )
}
