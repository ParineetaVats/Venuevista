import { useState } from 'react'
import PageHeader from '../components/PageHeader.jsx'
export default function Contact() {
  const [sent, setSent] = useState(false)
  return (
    <>
      <PageHeader color="#55747a" title="Contact us" sub="Questions about a venue or a booking? We reply within one working day." />
      <section className="page narrow">
        {sent ? <p className="notice" role="status">Message sent. We'll be in touch soon.</p> : (
          <form className="form" onSubmit={(e) => { e.preventDefault(); setSent(true) }}>
            <label>Name<input required /></label>
            <label>Email<input type="email" required /></label>
            <label>Message<textarea rows="5" required /></label>
            <button className="btn primary">Send message</button>
          </form>
        )}
        <p className="meta">hello@venuevista.example · Mon–Sat, 10am–7pm</p>
      </section>
    </>
  )
}
