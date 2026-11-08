import { request } from './http';

// Contrato combinado no #69. Formato das respostas esperado do backend:
//
// GroupContext:
//   { id, name, createdAt, studentCount, generatedAt | null }
//
// GroupGeneration (resultado de /generate e de /groups):
//   { contextId, contextName, generatedAt, maxAverageGap,
//     groups: [{
//       number,                       // 1, 2, ...
//       average,                      // média do perfil do grupo (1 a 5)
//       gapToOthers,                  // média do grupo − média dos outros grupos
//       members: [{ studentId, name, studentNumber }],
//       strongestByArea: [{ area: 'ORG' | 'TEMPO' | 'PROG' | 'EQUIPA', studentName }],
//     }] }
//
// RN26: os membros não trazem notas nem níveis. Mesmo que venham, o ecrã nunca os mostra.

const ERRORS = {
  400: 'Configuração impossível: os grupos têm de ter 4 ou 5 alunos.',
  404: 'Contexto ou aluno não encontrado.',
};

const json = (method, body) => ({
  method,
  headers: { 'Content-Type': 'application/json' },
  body: body === undefined ? undefined : JSON.stringify(body),
});

/**
 * POST /api/group-contexts
 * @param {{ name: string, studentIds: number[] }} context
 */
export function createGroupContext(context) {
  return request('/group-contexts', json('POST', context), ERRORS);
}

/**
 * GET /api/group-contexts
 * @returns {Promise<Array>} contextos e gerações anteriores
 */
export function getGroupContexts() {
  return request('/group-contexts', {}, ERRORS);
}

/**
 * POST /api/group-contexts/{id}/generate
 */
export function generateGroups(contextId) {
  return request(`/group-contexts/${contextId}/generate`, json('POST'), ERRORS);
}

/**
 * GET /api/group-contexts/{id}/groups
 */
export function getGroups(contextId) {
  return request(`/group-contexts/${contextId}/groups`, {}, ERRORS);
}
