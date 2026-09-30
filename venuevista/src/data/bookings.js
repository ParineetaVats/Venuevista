const K = 'vv-bookings2', D = 'vv-draft'
const rd = (k, d) => { try { return JSON.parse(localStorage.getItem(k)) ?? d } catch { return d } }
const wr = (k, v) => { try { localStorage.setItem(k, JSON.stringify(v)) } catch {} }
export const getBookings = () => rd(K, [])
export const saveBookings = (b) => wr(K, b)
export const addBooking = (b) => saveBookings([b, ...getBookings()])
export const getBooking = (id) => getBookings().find((b) => b.id === id)
export const getDraft = () => rd(D, null)
export const setDraft = (d) => wr(D, d)
export const clearDraft = () => { try { localStorage.removeItem(D) } catch {} }
export const displayStatus = (b) => (b.status === 'Confirmed' && b.date < new Date().toISOString().slice(0, 10) ? 'Completed' : b.status)
