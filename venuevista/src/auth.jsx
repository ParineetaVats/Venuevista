import { createContext, useContext, useState } from 'react'
const Ctx = createContext(null)
const read = (k, d) => { try { return JSON.parse(localStorage.getItem(k)) ?? d } catch { return d } }
const write = (k, v) => { try { localStorage.setItem(k, JSON.stringify(v)) } catch {} }
async function hash(s) {
  const b = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(s))
  return [...new Uint8Array(b)].map((x) => x.toString(16).padStart(2, '0')).join('')
}
// Demo auth: accounts live in this browser's localStorage. Replace with a real backend for production.
export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => read('vv-user', null))
  const start = (u) => { const { pw, ...safe } = u; write('vv-user', safe); setUser(safe) }
  const signup = async ({ password, confirm, ...rest }) => {
    const users = read('vv-users', [])
    const email = rest.email.trim().toLowerCase()
    if (users.some((u) => u.email === email)) return 'An account with this email already exists. Please log in.'
    const u = { ...rest, email, pw: await hash(password) }
    write('vv-users', [...users, u]); start(u); return null
  }
  const login = async (email, password) => {
    const u = read('vv-users', []).find((x) => x.email === email.trim().toLowerCase())
    if (!u) return 'No account found for this email. Please sign up first.'
    if (u.pw !== (await hash(password))) return 'Incorrect password. Please try again.'
    start(u); return null
  }
  const logout = () => { try { localStorage.removeItem('vv-user') } catch {} setUser(null) }
  return <Ctx.Provider value={{ user, signup, login, logout }}>{children}</Ctx.Provider>
}
export const useAuth = () => useContext(Ctx)
