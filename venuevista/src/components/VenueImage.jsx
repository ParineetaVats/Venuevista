// Illustrated venue "photos" (inline SVG, no downloads needed).
export default function VenueImage({ v }) {
  const id = 'g' + v.id
  const bulbs = Array.from({ length: 9 }, (_, i) => i)
  return (
    <svg viewBox="0 0 320 170" className="vimg" preserveAspectRatio="xMidYMid slice" role="img" aria-label={v.name}>
      <defs><linearGradient id={id} x1="0" y1="0" x2="0" y2="1"><stop offset="0" stopColor={v.c1} /><stop offset="1" stopColor={v.c2} /></linearGradient></defs>
      <rect width="320" height="170" fill={`url(#${id})`} />
      {v.type === 'Wedding' && <g>
        <path d="M95 170V88a65 65 0 0 1 130 0v82z" fill="rgba(255,255,255,.16)" stroke="#fff" strokeOpacity=".7" strokeWidth="3" />
        <path d="M120 170V95a40 40 0 0 1 80 0v75z" fill="rgba(0,0,0,.18)" />
        {bulbs.map((i) => <circle key={i} cx={40 + i * 30} cy={28 + Math.sin(i * 0.7) * 10} r="4" fill="#f3dfae" />)}
        {[60, 95, 225, 260].map((x) => <circle key={x} cx={x} cy="160" r="9" fill="#f0e6e0" fillOpacity=".85" />)}
      </g>}
      {v.type === 'Corporate' && <g>
        <rect x="40" y="40" width="110" height="130" fill="rgba(255,255,255,.14)" />
        <rect x="170" y="70" width="110" height="100" fill="rgba(255,255,255,.2)" />
        {Array.from({ length: 12 }, (_, i) => <rect key={i} x={54 + (i % 4) * 25} y={54 + Math.floor(i / 4) * 34} width="14" height="20" fill={i % 3 ? '#f3dfae' : 'rgba(255,255,255,.4)'} fillOpacity=".8" />)}
        {Array.from({ length: 8 }, (_, i) => <rect key={i} x={184 + (i % 4) * 25} y={84 + Math.floor(i / 4) * 34} width="14" height="20" fill="rgba(255,255,255,.4)" />)}
      </g>}
      {v.type === 'Party' && <g>
        <path d="M0 30Q160 90 320 30" stroke="#fff" strokeOpacity=".6" strokeWidth="2" fill="none" />
        {bulbs.map((i) => <circle key={i} cx={20 + i * 35} cy={30 + Math.sin((i / 8) * Math.PI) * 40} r="6" fill={['#f3dfae', '#c7d8e8', '#e3c4d0'][i % 3]} />)}
        <circle cx="160" cy="105" r="30" fill="rgba(255,255,255,.3)" stroke="#fff" strokeOpacity=".7" strokeWidth="2" />
        <path d="M130 105h60M160 75v60M139 84l42 42M181 84l-42 42" stroke="#fff" strokeOpacity=".5" />
      </g>}
      <rect y="150" width="320" height="20" fill="rgba(0,0,0,.18)" />
    </svg>
  )
}
