import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import PageHeader from '../components/PageHeader.jsx'
import { useAuth } from '../auth.jsx'
export default function Signup() {
  const { signup } = useAuth()
  const nav = useNavigate()
  const [errs, setErrs] = useState([])
  const [busy, setBusy] = useState(false)
  const submit = async (e) => {
    e.preventDefault()
    const d = Object.fromEntries(new FormData(e.target))
    const list = []
    if (d.name.trim().length < 2) list.push('Please enter your full name.')
    if (!/^[6-9]\d{9}$/.test(d.phone)) list.push('Enter a valid 10-digit mobile number.')
    if (d.password.length < 8 || !/[A-Za-z]/.test(d.password) || !/\d/.test(d.password)) list.push('Password needs 8+ characters with letters and numbers.')
    if (d.password !== d.confirm) list.push('Passwords do not match.')
    if (!d.agree) list.push('Please accept the terms to continue.')
    if (list.length) return setErrs(list)
    setBusy(true)
    const { agree, ...data } = d
    const msg = await signup(data)
    setBusy(false)
    if (msg) return setErrs([msg])
    nav('/venues')
  }
  return (
    <>
      <PageHeader color="#5f7f6a" title="Create your account" sub="Tell us a little about yourself so we can suggest the right venues." />
      <section className="page narrow">
        <form className="form" onSubmit={submit}>
          <div className="two">
            <label>Full name<input name="name" autoComplete="name" required /></label>
            <label>Mobile number<input name="phone" inputMode="numeric" maxLength="10" placeholder="10 digits" autoComplete="tel" required /></label>
          </div>
          <label>Email<input type="email" name="email" autoComplete="email" required /></label>
          <div className="two">
            <label>City<select name="city" defaultValue="Delhi">{['Delhi', 'Gurugram', 'Noida', 'Ghaziabad', 'Faridabad', 'Other'].map((c) => <option key={c}>{c}</option>)}</select></label>
            <label>I'm planning a<select name="interest" defaultValue="Wedding">{['Wedding', 'Corporate event', 'Party', 'Other'].map((c) => <option key={c}>{c}</option>)}</select></label>
          </div>
          <div className="two">
            <label>Expected guests<input type="number" name="guests" min="1" max="5000" placeholder="e.g. 150" required /></label>
            <label>Approx. budget (₹)<input type="number" name="budget" min="0" step="1000" placeholder="e.g. 60000" /></label>
          </div>
          <div className="two">
            <label>Password<input type="password" name="password" autoComplete="new-password" required /></label>
            <label>Confirm password<input type="password" name="confirm" autoComplete="new-password" required /></label>
          </div>
          <label className="check"><input type="checkbox" name="agree" /> I agree to the terms and privacy policy</label>
          {errs.length > 0 && <ul className="err" role="alert">{errs.map((x) => <li key={x}>{x}</li>)}</ul>}
          <button className="btn primary" disabled={busy}>{busy ? 'Creating account…' : 'Sign up'}</button>
        </form>
        <p className="meta">Already have an account? <Link to="/login">Log in</Link></p>
      </section>
    </>
  )
}
