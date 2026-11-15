// Tema escolhido no botão de modo noturno (só neste browser).
const KEY = 'studymatch.theme';
const DARK_QUERY = '(prefers-color-scheme: dark)';

export function readStoredTheme() {
  try {
    const value = window.localStorage.getItem(KEY);
    return value === 'dark' || value === 'light' ? value : null;
  } catch {
    return null;
  }
}

export function storeTheme(theme) {
  try {
    window.localStorage.setItem(KEY, theme);
  } catch {
    // Sem armazenamento: o tema dura só até fechar a página.
  }
}

export function systemTheme() {
  return typeof window.matchMedia === 'function' && window.matchMedia(DARK_QUERY).matches ? 'dark' : 'light';
}

/**
 * Aplica o tema guardado antes de a app aparecer (evita piscar). Sem escolha, segue o sistema.
 */
export function applyStoredTheme() {
  const stored = readStoredTheme();
  if (stored) document.documentElement.dataset.theme = stored;
}
