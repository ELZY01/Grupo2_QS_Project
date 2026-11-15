/**
 * Sessão guardada no browser, depois do login no backend (POST /api/auth/login).
 *
 * A sessão só guarda o token e os dados para mostrar; quem decide as permissões é o backend.
 * Roles: 'admin', 'docente' e 'student'.
 */
const KEY = 'studymatch.session';

export const SESSION_EXPIRED_EVENT = 'studymatch:session-expired';

// Role do backend → role usada no frontend.
const ROLES = { ADMIN: 'admin', DOCENTE: 'docente', ALUNO: 'student' };

/**
 * Converte a resposta do login/registo numa sessão do frontend.
 */
export function sessionFromLogin(response) {
  return {
    role: ROLES[response.role],
    token: response.token,
    id: response.studentId ?? null,
    userId: response.userId,
    name: response.displayName,
  };
}

export function readSession() {
  try {
    const raw = window.localStorage.getItem(KEY);
    const session = raw ? JSON.parse(raw) : null;
    if (!session?.token || !Object.values(ROLES).includes(session.role)) return null;
    if (session.role === 'student' && !Number.isFinite(session.id)) return null;
    return session;
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

/**
 * Token da sessão atual, ou null.
 */
export function getToken() {
  return readSession()?.token ?? null;
}
