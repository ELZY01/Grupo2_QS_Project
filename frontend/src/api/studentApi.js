const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';
const STUDENTS_URL = `${API_URL}/api/students`;

// Mensagens usadas quando a API não envia o texto do erro.
const DEFAULT_ERRORS = {
  400: 'Os dados do estudante são inválidos.',
  404: 'Estudante não encontrado.',
  409: 'O email ou o número de estudante já está registado.',
};

/**
 * Erro devolvido pela API, com o código HTTP e a mensagem a mostrar.
 */
export class ApiError extends Error {
  constructor(status, message) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

/**
 * Lê a resposta da API e lança ApiError quando o pedido falha.
 */
async function handleResponse(response) {
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

  throw new ApiError(
    response.status,
    message || DEFAULT_ERRORS[response.status] || `Erro inesperado (HTTP ${response.status}).`
  );
}

/**
 * GET /api/students
 * @returns {Promise<Array>} lista de estudantes
 */
export async function getStudents() {
  const response = await fetch(STUDENTS_URL);
  return handleResponse(response);
}

/**
 * POST /api/students
 * @param {object} student dados do novo estudante
 */
export async function createStudent(student) {
  const response = await fetch(STUDENTS_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(student),
  });
  return handleResponse(response);
}

/**
 * PUT /api/students/{id}
 * @param {number} id identificador do estudante
 * @param {object} student dados atualizados
 */
export async function updateStudent(id, student) {
  const response = await fetch(`${STUDENTS_URL}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(student),
  });
  return handleResponse(response);
}
