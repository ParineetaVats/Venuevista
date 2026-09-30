import { Routes, Route, Navigate, Outlet, useLocation } from 'react-router-dom'
import Footer from './components/Footer.jsx'
import EventDetails from './pages/EventDetails.jsx'
import SeatSelection from './pages/SeatSelection.jsx'
import Checkout from './pages/Checkout.jsx'
import Confirmation from './pages/Confirmation.jsx'
import MyTickets from './pages/MyTickets.jsx'
import AdminLayout from './admin/AdminLayout.jsx'
import TicketSales from './admin/TicketSales.jsx'
import Analytics from './admin/Analytics.jsx'
import SeatingManagement from './admin/SeatingManagement.jsx'
import Navbar from './components/Navbar.jsx'
import ScrollToTop from './components/ScrollToTop.jsx'
import Home from './pages/Home.jsx'
import About from './pages/About.jsx'
import Events from './pages/Events.jsx'
import Venues from './pages/Venues.jsx'
import MyBookings from './pages/MyBookings.jsx'
import Contact from './pages/Contact.jsx'
import Signup from './pages/Signup.jsx'
import { AuthProvider } from './auth.jsx'
import Login from './pages/Login.jsx'
import NotFound from './pages/NotFound.jsx'

function Layout() {
  const { pathname } = useLocation()
  return (
    <>
      <ScrollToTop />
      <Navbar />
      <main key={pathname}><Outlet /></main>
      <Footer />
    </>
  )
}

export default function App() {
  return (
    <AuthProvider>
    <Routes>
      <Route path="/admin" element={<AdminLayout />}>
        <Route index element={<Navigate to="ticket-sales" replace />} />
        <Route path="ticket-sales" element={<TicketSales />} />
        <Route path="analytics" element={<Analytics />} />
        <Route path="seating" element={<SeatingManagement />} />
      </Route>
      <Route element={<Layout />}>
        <Route path="/events/:id/seats" element={<SeatSelection />} />
        <Route path="/checkout" element={<Checkout />} />
        <Route path="/booking-confirmation/:id" element={<Confirmation />} />
        <Route path="/my-tickets" element={<MyTickets />} />
        <Route path="/" element={<Home />} />
        <Route path="/about" element={<About />} />
        <Route path="/events" element={<Events />} />
        <Route path="/events/:id" element={<EventDetails />} />
        <Route path="/venues" element={<Venues />} />
        <Route path="/my-bookings" element={<MyBookings />} />
        <Route path="/contact" element={<Contact />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />
        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
    </AuthProvider>
  )
}
