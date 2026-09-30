import { Link } from 'react-router-dom'
import PageHeader from '../components/PageHeader.jsx'
export default function NotFound() {
  return (
    <>
      <PageHeader color="#6b5b5b" title="404: page not found" sub="That address doesn't exist. It may have moved or been mistyped." />
      <section className="page narrow empty"><Link to="/" className="btn primary">Back to Home</Link></section>
    </>
  )
}
