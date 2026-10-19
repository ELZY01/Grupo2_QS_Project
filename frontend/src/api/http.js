const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

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
  const response = await fetch(`${API_URL}/api${path}`, options);

  if (response.ok) {
    return response.json();
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
