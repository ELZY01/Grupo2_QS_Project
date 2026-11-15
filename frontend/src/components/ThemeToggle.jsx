import { useEffect, useState } from 'react';
import { readStoredTheme, storeTheme, systemTheme } from '../auth/theme.js';
import './ThemeToggle.css';

/**
 * Botão de modo noturno: de dia mostra o sol, à noite a lua com estrelas.
 * A escolha fica neste browser; sem escolha, segue o tema do sistema.
 */
export default function ThemeToggle() {
  const [theme, setTheme] = useState(() => readStoredTheme() ?? systemTheme());

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
  }, [theme]);

  const toggle = () => {
    const next = theme === 'dark' ? 'light' : 'dark';
    storeTheme(next);
    setTheme(next);
  };

  const night = theme === 'dark';

  return (
    <button
      type="button"
      className={`theme-toggle ${night ? 'is-night' : 'is-day'}`}
      onClick={toggle}
      aria-pressed={night}
      aria-label={night ? 'Mudar para o modo de dia' : 'Mudar para o modo noturno'}
    >
      <span className="theme-toggle-sky" aria-hidden="true">
        <span className="theme-toggle-body" />
        <span className="theme-toggle-stars" />
      </span>
      <span className="theme-toggle-label">{night ? 'Noite' : 'Dia'}</span>
    </button>
  );
}
