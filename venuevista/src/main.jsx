import React from 'react'
import ReactDOM from 'react-dom/client'
import { HashRouter } from 'react-router-dom'
import App from './App.jsx'
import './index.css'
// HashRouter keeps navigation reliable in dev servers/previews (URLs look like /#/about).
// To use clean URLs (/about) swap HashRouter for BrowserRouter.
ReactDOM.createRoot(document.getElementById('root')).render(<HashRouter><App /></HashRouter>)
