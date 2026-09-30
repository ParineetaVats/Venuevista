import { IndianRupee, Ticket, Tag, Percent, Armchair } from 'lucide-react'
import { ResponsiveContainer, LineChart, Line, BarChart, Bar, PieChart, Pie, Cell, XAxis, YAxis, Tooltip, CartesianGrid, Legend } from 'recharts'
import { events } from '../data/events.js'
import { venues, venueById, inr } from '../data.js'
import { getBookings } from '../data/bookings.js'
import { eventSales, monthly } from '../data/seating.js'
import { StatCard, ChartCard } from '../components/ChartCard.jsx'
const C = ['#2f4858', '#c8a45c', '#7c6bb0', '#5f8fa3', '#2f7d54', '#a3331f']
const k = (n) => (n >= 1e5 ? (n / 1e5).toFixed(1) + 'L' : n)
export default function Analytics() {
  const bk = getBookings()
  const rows = events.map((e) => ({ e, ...eventSales(e, bk) }))
  const rev = rows.reduce((n, r) => n + r.revenue, 0), sold = rows.reduce((n, r) => n + r.sold, 0), cap = rows.reduce((n, r) => n + r.e.seats, 0)
  const byEvent = rows.map((r) => ({ name: r.e.title.split(' ').slice(0, 2).join(' '), revenue: r.revenue, occ: Math.round((r.sold / r.e.seats) * 100) }))
  const byVenue = venues.map((v) => ({ name: v.name.split(' ').slice(0, 2).join(' '), revenue: rows.filter((r) => r.e.venue === v.id).reduce((n, r) => n + r.revenue, 0) })).filter((v) => v.revenue)
  const cat = [{ name: 'Standard', value: 58 }, { name: 'Premium', value: 27 }, { name: 'VIP', value: 15 }]
  const top = [...rows].sort((a, b) => b.revenue - a.revenue).slice(0, 5)
  const topV = [...byVenue].sort((a, b) => b.revenue - a.revenue).slice(0, 5)
  const axis = { tick: { fontSize: 12 } }
  return (
    <>
      <h1>Sales analytics</h1>
      <div className="vv-grid four">
        <StatCard icon={IndianRupee} label="Total revenue" value={inr(rev)} />
        <StatCard icon={Ticket} label="Tickets sold" value={sold.toLocaleString('en-IN')} />
        <StatCard icon={Tag} label="Avg. ticket price" value={inr(Math.round(rev / sold))} />
        <StatCard icon={Percent} label="Booking conversion" value="4.6%" note="visitors → bookings" />
        <StatCard icon={Armchair} label="Seat occupancy" value={Math.round((sold / cap) * 100) + '%'} />
      </div>
      <div className="vv-grid ad-charts">
        <ChartCard title="Revenue over time" sub="2026, per month"><ResponsiveContainer><LineChart data={monthly}><CartesianGrid strokeDasharray="3 3" /><XAxis dataKey="m" {...axis} /><YAxis tickFormatter={k} {...axis} /><Tooltip formatter={(v) => inr(v)} /><Line dataKey="revenue" stroke={C[0]} strokeWidth={3} dot /></LineChart></ResponsiveContainer></ChartCard>
        <ChartCard title="Tickets sold over time"><ResponsiveContainer><BarChart data={monthly}><CartesianGrid strokeDasharray="3 3" /><XAxis dataKey="m" {...axis} /><YAxis {...axis} /><Tooltip /><Bar dataKey="tickets" fill={C[1]} radius={[6, 6, 0, 0]} /></BarChart></ResponsiveContainer></ChartCard>
        <ChartCard title="Revenue by event"><ResponsiveContainer><BarChart data={byEvent}><CartesianGrid strokeDasharray="3 3" /><XAxis dataKey="name" {...axis} interval={0} angle={-25} textAnchor="end" height={60} /><YAxis tickFormatter={k} {...axis} /><Tooltip formatter={(v) => inr(v)} /><Bar dataKey="revenue" fill={C[2]} radius={[6, 6, 0, 0]} /></BarChart></ResponsiveContainer></ChartCard>
        <ChartCard title="Revenue by venue"><ResponsiveContainer><BarChart data={byVenue} layout="vertical"><CartesianGrid strokeDasharray="3 3" /><XAxis type="number" tickFormatter={k} {...axis} /><YAxis type="category" dataKey="name" width={110} {...axis} /><Tooltip formatter={(v) => inr(v)} /><Bar dataKey="revenue" fill={C[3]} radius={[0, 6, 6, 0]} /></BarChart></ResponsiveContainer></ChartCard>
        <ChartCard title="Ticket category distribution"><ResponsiveContainer><PieChart><Pie data={cat} dataKey="value" nameKey="name" innerRadius={55} outerRadius={90} label>{cat.map((_, i) => <Cell key={i} fill={C[i]} />)}</Pie><Legend /><Tooltip formatter={(v) => v + '%'} /></PieChart></ResponsiveContainer></ChartCard>
        <ChartCard title="Seat occupancy by event" sub="% of seats sold"><ResponsiveContainer><BarChart data={byEvent}><CartesianGrid strokeDasharray="3 3" /><XAxis dataKey="name" {...axis} interval={0} angle={-25} textAnchor="end" height={60} /><YAxis domain={[0, 100]} {...axis} /><Tooltip formatter={(v) => v + '%'} /><Bar dataKey="occ" fill={C[4]} radius={[6, 6, 0, 0]} /></BarChart></ResponsiveContainer></ChartCard>
      </div>
      <div className="vv-grid">
        <div className="card body"><h3>Top events</h3><ol className="rank">{top.map((r) => <li key={r.e.id}><span>{r.e.title}</span><b>{inr(r.revenue)}</b></li>)}</ol></div>
        <div className="card body"><h3>Top venues</h3><ol className="rank">{topV.map((v) => <li key={v.name}><span>{v.name}</span><b>{inr(v.revenue)}</b></li>)}</ol></div>
      </div>
    </>
  )
}
