import { Music, Trophy, Presentation, Palette, Laugh, Drama, PartyPopper, Briefcase } from 'lucide-react'
import { catStyle } from '../data/events.js'
const icons = { Concerts: Music, Sports: Trophy, Conferences: Presentation, Workshops: Palette, Comedy: Laugh, Theatre: Drama, Festivals: PartyPopper, 'Corporate Events': Briefcase }
// Self-contained illustrated banner per category (no remote images, never breaks).
export default function EventImage({ category, height = 190, label }) {
  const [a, b] = catStyle[category] || catStyle['Corporate Events']
  const Icon = icons[category] || Briefcase
  return (
    <div className="ev-art" style={{ height, background: `radial-gradient(circle at 80% 15%, ${b}aa, transparent 55%), linear-gradient(135deg, ${a}, ${b})` }} role="img" aria-label={label || category}>
      <Icon size={Math.min(96, height / 2)} strokeWidth={1.2} />
    </div>
  )
}
