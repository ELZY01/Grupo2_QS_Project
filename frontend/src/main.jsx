import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import { applyStoredTheme } from './auth/theme.js'
import ThemeToggle from './components/ThemeToggle.jsx'

applyStoredTheme()

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
    <ThemeToggle />
  </StrictMode>,
)
