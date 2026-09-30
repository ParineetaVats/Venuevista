export default function Logo({ size = 40, light = false }) {
  return (
    <svg width={size} height={size} viewBox="0 0 48 48" role="img" aria-label="VenueVista logo">
      <rect width="48" height="48" rx="13" fill={light ? '#fff' : '#2f4858'} />
      <path d="M24 8c-6.6 0-12 5.2-12 11.7C12 28 24 41 24 41s12-13 12-21.3C36 13.2 30.600 8 24 8z" fill="none" stroke={light ? '#2f4858' : '#fff'} strokeWidth="2.6" strokeLinejoin="round" />
      <path d="M17.500 16.500h13a1 1 0 0 1 1 1v2.300a2 2 0 0 0 0 4v2.300a1 1 0 0 1-1 1h-13a1 1 0 0 1-1-1v-2.300a2 2 0 0 0 0-4V17.500a1 1 0 0 1 1-1z" fill="#d4b170" />
    </svg>
  )
}
export function Brand({ tagline = false, light = false }) {
  return (
    <span className="brand">
      <Logo light={light} />
      <span><b>Venue<i>Vista</i></b>{tagline && <small>EventPro Ticketing &amp; Venue Management</small>}</span>
    </span>
  )
}
