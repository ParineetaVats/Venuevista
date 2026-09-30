export default function SeatingChart({ layout, booked = new Set(), selected = new Set(), onSeat, admin = false }) {
  let prev = null
  return (
    <div className="sc">
      <div className="sc-stage">STAGE</div>
      <div className="sc-scroll"><div className="sc-rows">
        {layout.rows.map((r) => {
          const t = r.seats[0]?.type, label = t !== prev; prev = t
          return (
            <div key={r.id}>
              {label && <div className={`sc-sec ${t}`}>{t} section</div>}
              <div className="sc-row"><span className="sc-rl">{r.id}</span>
                {r.seats.map((s) => {
                  const id = r.id + s.n, isB = booked.has(id), sel = selected.has(id)
                  const cls = ['seat', s.type, isB && 'booked', s.off && 'off', sel && 'sel'].filter(Boolean).join(' ')
                  const state = sel ? 'selected' : s.off ? 'unavailable' : isB ? 'booked' : 'available'
                  return <button key={id} type="button" className={cls} disabled={!admin && (isB || s.off)} aria-pressed={sel} aria-label={`Seat ${id}, ${s.type}, ${state}`} title={`${id} · ${s.type} · ${state}`} onClick={() => onSeat && onSeat(r, s, id)}>{s.n}</button>
                })}
              </div>
            </div>
          )
        })}
      </div></div>
    </div>
  )
}
export function Legend({ prices }) {
  return (
    <div className="sc-legend">
      {['VIP', 'Premium', 'Standard'].map((t) => <span key={t}><i className={`seat ${t}`} />{t}{prices ? ` ${prices[t]}` : ''}</span>)}
      <span><i className="seat sel" />Selected</span><span><i className="seat booked" />Booked</span><span><i className="seat off" />Unavailable</span>
    </div>
  )
}
