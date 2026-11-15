import { request } from './http';

const json = (method, body) => ({
  method,
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(body),
});

/**
 * POST /api/auth/login
 * O aluno pode usar o número de estudante ou o email.
 * @returns {Promise<{token, role, userId, studentId, displayName}>}
 */
export function login(username, password) {
  return request('/auth/login', json('POST', { username, password }), {
    401: 'Utilizador ou palavra-passe errados.',
    429: 'Demasiadas tentativas falhadas. Tenta de novo daqui a alguns minutos.',
  });
}

/**
 * POST /api/auth/register
 * Regista o aluno com palavra-passe e devolve a sessão (como o login).
 */
export function register(student) {
  return request('/auth/register', json('POST', student), {
    400: 'Os dados do registo são inválidos.',
    409: 'O email ou o número de estudante já está registado.',
  });
}

/**
 * POST /api/auth/logout
 * Invalida o token no backend. Erros são ignorados (a sessão local é apagada na mesma).
 */
export async function logout() {
  try {
    await request('/auth/logout', { method: 'POST' });
  } catch {
    // Sem rede ou token já expirado: não há nada a fazer.
  }
}

/**
 * PUT /api/admin/students/{studentId}/password
 * O admin define a palavra-passe do aluno (cria a conta se ainda não existir).
 */
export async function setStudentPassword(studentId, password) {
  await request(`/admin/students/${studentId}/password`, json('PUT', { password }), {
    400: 'A palavra-passe tem de ter pelo menos 8 caracteres.',
    404: 'Estudante não encontrado.',
  });
}
