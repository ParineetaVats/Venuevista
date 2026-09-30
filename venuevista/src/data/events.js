export const categories = ['Concerts', 'Sports', 'Conferences', 'Workshops', 'Comedy', 'Theatre', 'Festivals', 'Corporate Events']
export const catStyle = {
  Concerts: ['#5b2a86', '#e2559d'], Sports: ['#0f6b4f', '#7fd36b'], Conferences: ['#1f3b73', '#5aa2e6'],
  Workshops: ['#8a4b1f', '#f0b35c'], Comedy: ['#a3331f', '#f5a04a'], Theatre: ['#4a1d3c', '#c1517d'],
  Festivals: ['#b3541e', '#f7c948'], 'Corporate Events': ['#2f4858', '#86a8c4'],
}
// venue = id from data.js venues; seats = total capacity for this event
export const events = [
  { id: 1, title: 'Sufi Nights Live', category: 'Concerts', date: '2026-11-14', time: '7:00 PM', venue: 3, price: 1499, seats: 900, left: 620, featured: true, organizer: 'Raag Live Entertainment', desc: 'An open-air evening of Sufi and qawwali classics performed by an ensemble of award-winning artists under the stars.' },
  { id: 2, title: 'Capital Cricket League Finale', category: 'Sports', date: '2026-12-06', time: '6:30 PM', venue: 3, price: 799, seats: 900, left: 540, featured: true, organizer: 'Capital Sports Board', desc: 'The season-ending showdown between the two top-ranked city teams, with live commentary and fan zones.' },
  { id: 3, title: 'Product Design Summit', category: 'Conferences', date: '2026-12-05', time: '9:30 AM', venue: 5, price: 2499, seats: 250, left: 140, featured: true, organizer: 'DesignForward India', desc: 'A full-day summit with keynotes, panels and portfolio reviews from product leaders across India.' },
  { id: 4, title: 'Pottery & Ceramics Workshop', category: 'Workshops', date: '2026-10-24', time: '11:00 AM', venue: 4, price: 899, seats: 40, left: 32, featured: false, organizer: 'Clay Studio Collective', desc: 'A hands-on beginner workshop: throw your first pot on the wheel and take your creation home.' },
  { id: 5, title: 'Laugh Riot: Stand-up Special', category: 'Comedy', date: '2026-10-30', time: '8:00 PM', venue: 8, price: 599, seats: 150, left: 96, featured: true, organizer: 'Punchline Productions', desc: 'Four of the country\'s sharpest stand-up comics take the rooftop stage for one unforgettable night.' },
  { id: 6, title: 'The Last Ember', category: 'Theatre', date: '2026-11-21', time: '7:30 PM', venue: 7, price: 999, seats: 400, left: 210, featured: false, organizer: 'Rangmanch Theatre Group', desc: 'A critically acclaimed period drama about a family holding on to its legacy, staged in a heritage courtyard.' },
  { id: 7, title: 'Diwali Lights Festival', category: 'Festivals', date: '2026-11-08', time: '5:00 PM', venue: 6, price: 499, seats: 300, left: 260, featured: false, organizer: 'Utsav Events', desc: 'Food stalls, folk performances, craft markets and a spectacular lights show to celebrate the festival of lights.' },
  { id: 8, title: 'Startup Founders Summit', category: 'Corporate Events', date: '2026-10-12', time: '10:00 AM', venue: 2, price: 1999, seats: 120, left: 70, featured: false, organizer: 'FoundersCircle', desc: 'Meet investors, mentors and fellow founders through curated talks and structured networking sessions.' },
]
export const eventById = (id) => events.find((e) => e.id === Number(id))
export const fmtDate = (d) => new Date(d + 'T00:00:00').toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })
