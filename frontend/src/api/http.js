import { SESSION_EXPIRED_EVENT, getToken } from '../auth/session.js';

export const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

/**
 * fetch com o token da sessão no cabeçalho Authorization.
 * Sem sessão faz um fetch normal. Se o backend responder 401 com sessão, avisa a app
 * (o token expirou ou o backend reiniciou) para voltar ao ecrã de entrada.
 */
export async function apiFetch(url, options) {
  const token = getToken();
  if (!token) return options === undefined ? fetch(url) : fetch(url, options);

  const response = await fetch(url, {
    ...options,
    headers: { ...(options?.headers ?? {}), Authorization: `Bearer ${token}` },
  });
  if (response.status === 401) {
    window.dispatchEvent(new Event(SESSION_EXPIRED_EVENT));
  }
  return response;
}

/**
 * Erro devolvido pela API, com o código HTTP e a mensagem a mostrar.
 */
export class HttpError extends Error {
  constructor(status, message) {
    super(message);
    this.name = 'HttpError';
    this.status = status;
  }
}

/**
 * Faz um pedido à API e devolve o JSON da resposta.
 * Quando o pedido falha, lança HttpError com a mensagem enviada pela API
 * (campo `message`) ou com a mensagem por omissão para esse código HTTP.
 *
 * @param {string} path caminho a partir de /api (ex.: '/course-units')
 * @param {object} options opções do fetch
 * @param {object} defaultErrors mensagens por código HTTP
 */
export async function request(path, options = {}, defaultErrors = {}) {
  const response = await apiFetch(`${API_URL}/api${path}`, options);

  if (response.ok) {
    // 204 No Content (ex.: logout, definir palavra-passe) não tem corpo.
    return response.status === 204 ? null : response.json();
  }

  let message;
  try {
    const body = await response.json();
    message = body.message || body.detail;
  } catch {
    // A resposta não tem JSON; usa a mensagem por omissão.
  }

  throw new HttpError(
    response.status,
    message || defaultErrors[response.status] || `Erro inesperado (HTTP ${response.status}).`
  );
}
