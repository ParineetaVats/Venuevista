import { useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import PageHeader from '../components/PageHeader.jsx'
import { useAuth } from '../auth.jsx'
export default function Login() {
  const { login } = useAuth()
  const nav = useNavigate()
  const from = useLocation().state?.from || '/my-bookings'
  const [err, setErr] = useState('')
  const [busy, setBusy] = useState(false)
  const submit = async (e) => {
    e.preventDefault()
    const f = new FormData(e.target)
    setBusy(true)
    const msg = await login(f.get('email'), f.get('password'))
    setBusy(false)
    if (msg) return setErr(msg)
    nav(from)
  }
  return (
    <>
      <PageHeader color="#4a5568" title="Welcome back" sub="Log in to book venues and manage your bookings." />
      <section className="page narrow">
        <form className="form" onSubmit={submit}>
          <label>Email<input type="email" name="email" autoComplete="email" required /></label>
          <label>Password<input type="password" name="password" autoComplete="current-password" required /></label>
          {err && <p className="err" role="alert">{err}</p>}
          <button className="btn primary" disabled={busy}>{busy ? 'Logging in…' : 'Log in'}</button>
        </form>
        <p className="meta">New to VenueVista? <Link to="/signup">Create an account</Link></p>
      </section>
    </>
  )
}
