import { useMemo, useState } from 'react'
import { venues } from '../data.js'
import { events } from '../data/events.js'
import { getLayout, saveLayout, defaultLayout, bookedSeats, types } from '../data/seating.js'
import SeatingChart, { Legend } from '../components/SeatingChart.jsx'
import { StatCard } from '../components/ChartCard.jsx'
import { Armchair, CheckCircle2, Ticket, Ban } from 'lucide-react'
export default function SeatingManagement() {
  const [vid, setVid] = useState(1), [layout, setLayout] = useState(() => getLayout(1)), [sel, setSel] = useState(null)
  const evs = events.filter((e) => e.venue === vid), [eid, setEid] = useState('')
  const ev = evs.find((e) => e.id === +eid) || evs[0]
  const booked = useMemo(() => (ev ? bookedSeats(ev) : new Set()), [ev, layout])
  const commit = (next) => { saveLayout(vid, next); setLayout(next) }
  const pick = (v) => { setVid(v); setLayout(getLayout(v)); setSel(null); setEid('') }
  const seatOf = (rid, n) => layout.rows.find((r) => r.id === rid)?.seats.find((s) => s.n === n)
  const edit = (fn) => commit({ rows: layout.rows.map((r) => ({ ...r, seats: r.seats.map((s) => ({ ...s })) })).map(fn) })
  const all = layout.rows.flatMap((r) => r.seats.map((s) => ({ ...s, id: r.id + s.n })))
  const off = all.filter((s) => s.off).length, bk = all.filter((s) => booked.has(s.id) && !s.off).length, avail = all.length - off - bk
  const cur = sel && seatOf(sel.r, sel.n)
  return (
    <>
      <h1>Seating management</h1>
      <div className="ev-filters ad-f">
        <select aria-label="Venue" value={vid} onChange={(e) => pick(+e.target.value)}>{venues.map((v) => <option key={v.id} value={v.id}>{v.name}</option>)}</select>
        {evs.length ? <select aria-label="Event for occupancy" value={ev.id} onChange={(e) => setEid(e.target.value)}>{evs.map((e) => <option key={e.id} value={e.id}>Occupancy: {e.title}</option>)}</select> : <span className="meta">No events at this venue yet.</span>}
      </div>
      <div className="vv-grid four">
        <StatCard icon={Armchair} label="Total seats" value={all.length} />
        <StatCard icon={CheckCircle2} label="Available" value={avail} />
        <StatCard icon={Ticket} label="Booked" value={bk} note={all.length ? Math.round((bk / all.length) * 100) + '% occupancy' : ''} />
        <StatCard icon={Ban} label="Unavailable" value={off} />
      </div>
      <div className="sc-wrap">
        <div className="card body"><SeatingChart admin layout={layout} booked={booked} selected={new Set(sel ? [sel.r + sel.n] : [])} onSeat={(r, s) => setSel({ r: r.id, n: s.n })} /><Legend /></div>
        <aside className="card body sc-sum">
          <h3>Layout controls</h3>
          <button className="btn sm" disabled={layout.rows.length >= 12} onClick={() => commit({ rows: [...layout.rows, { id: String.fromCharCode(65 + layout.rows.length), seats: Array.from({ length: 10 }, (_, i) => ({ n: i + 1, type: 'Standard', off: false })) }] })}>+ Add row</button>
          <button className="btn sm" disabled={!sel} onClick={() => edit((r) => { if (r.id === sel.r) r.seats.push({ n: Math.max(0, ...r.seats.map((s) => s.n)) + 1, type: r.seats.at(-1)?.type || 'Standard', off: false }); return r })}>+ Add seat to row {sel?.r || ''}</button>
          <hr />
          {cur ? <>
            <p><b>Seat {sel.r}{sel.n}</b></p>
            <label>Seat type<select value={cur.type} onChange={(e) => edit((r) => { if (r.id === sel.r) r.seats.forEach((s) => { if (s.n === sel.n) s.type = e.target.value }); return r })}>{types.map((t) => <option key={t}>{t}</option>)}</select></label>
            <button className="btn sm" onClick={() => edit((r) => { if (r.id === sel.r) r.seats.forEach((s) => { if (s.n === sel.n) s.off = !s.off }); return r })}>{cur.off ? 'Mark available' : 'Mark unavailable'}</button>
            <button className="btn sm" onClick={() => edit((r) => { if (r.id === sel.r) r.seats.forEach((s) => { s.type = cur.type }); return r })}>Apply {cur.type} to whole row</button>
          </> : <p className="meta">Click a seat to change its type or availability.</p>}
          <hr /><button className="btn sm" onClick={() => { commit(defaultLayout()); setSel(null) }}>Reset layout</button>
          <small className="meta">Changes are saved and used on the public seat-selection page.</small>
        </aside>
      </div>
    </>
  )
}
