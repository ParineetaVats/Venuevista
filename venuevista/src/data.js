export const venues = [
  { id: 1, name: 'Lotus Grand Ballroom', city: 'Delhi', type: 'Wedding', capacity: 600, price: 85000, c1: '#4b5d78', c2: '#a9b8c9' },
  { id: 2, name: 'The Terrace Loft', city: 'Gurugram', type: 'Corporate', capacity: 120, price: 32000, c1: '#2f4858', c2: '#6f8f9b' },
  { id: 3, name: 'Yamuna Lawn Resort', city: 'Noida', type: 'Wedding', capacity: 900, price: 120000, c1: '#4f6b58', c2: '#a5bfa0' },
  { id: 4, name: 'Studio Nine', city: 'Delhi', type: 'Party', capacity: 80, price: 18000, c1: '#5b4d6b', c2: '#a695b8' },
  { id: 5, name: 'Summit Conference Hall', city: 'Ghaziabad', type: 'Corporate', capacity: 250, price: 45000, c1: '#3b4a5a', c2: '#8a9bab' },
  { id: 6, name: 'Saffron Banquet', city: 'Ghaziabad', type: 'Party', capacity: 300, price: 52000, c1: '#7c6548', c2: '#d1b98f' },
  { id: 7, name: 'Heritage Haveli Courtyard', city: 'Delhi', type: 'Wedding', capacity: 400, price: 95000, c1: '#6b4f5a', c2: '#c4a3ab' },
  { id: 8, name: 'Skyline Rooftop Lounge', city: 'Gurugram', type: 'Party', capacity: 150, price: 40000, c1: '#34506a', c2: '#86a8c4' },
]
export const venueById = (id) => venues.find((v) => v.id === id)
export const inr = (n) => '₹' + n.toLocaleString('en-IN')
export const getBookings = () => { try { return JSON.parse(localStorage.getItem('vv-bookings') || '[]') } catch { return [] } }
export const saveBookings = (b) => { try { localStorage.setItem('vv-bookings', JSON.stringify(b)) } catch {} }
