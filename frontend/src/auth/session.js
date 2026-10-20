/**
 * Sessão guardada no browser.
 *
 * ATENÇÃO: o backend ainda não tem autenticação. O administrador é validado só no
 * frontend, com as credenciais de demonstração abaixo (podem ser trocadas no .env).
 * Para produção é preciso autenticação no backend (Spring Security).
 */
export const ADMIN_USER = import.meta.env.VITE_ADMIN_USER ?? 'admin';
export const ADMIN_PASSWORD = import.meta.env.VITE_ADMIN_PASSWORD ?? 'studymatch';

const KEY = 'studymatch.session';

export function readSession() {
  try {
    const raw = window.localStorage.getItem(KEY);
    const session = raw ? JSON.parse(raw) : null;
    if (session?.role === 'admin') return { role: 'admin' };
    if (session?.role === 'student' && Number.isFinite(session.id)) return session;
    return null;
  } catch {
    return null;
  }
}

export function saveSession(session) {
  try {
    if (session) window.localStorage.setItem(KEY, JSON.stringify(session));
    else window.localStorage.removeItem(KEY);
  } catch {
    // Sem armazenamento: a sessão dura só até fechar a página.
  }
}

export const isAdminLogin = (user, password) => user === ADMIN_USER && password === ADMIN_PASSWORD;
