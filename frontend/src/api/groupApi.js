import { request } from './http';

// Endpoints do admin, sem UC (#69). O backend responde assim:
//
// GroupContextResponse:
//   { id, name, createdAt, generatedAt | null, generations: [2, 1] }
//
// /generate devolve os grupos da geração nova e /groups os de todas as gerações:
//   [{ id, generationNumber, groupNumber, average, gapToOthers,
//      members: [{ studentId, studentNumber, name }] }]
//
// Os ecrãs usam uma geração de cada vez (ver toGeneration):
//   { generationNumber, generations, maxAverageGap,
//     groups: [{ id, number, average, gapToOthers, members }] }
//
// RN26: os membros não trazem notas nem níveis. Mesmo que venham, o ecrã nunca os mostra.

/** Limite de equilíbrio entre médias de grupos (RN24, igual ao GroupFormation do backend). */
export const MAX_AVERAGE_GAP = 0.5;

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
 * Junta os grupos de uma geração no formato dos ecrãs.
 *
 * @param {Array} groups grupos devolvidos pelo backend (#69)
 * @param {number} [generationNumber] geração a mostrar; por omissão a mais recente
 */
export function toGeneration(groups, generationNumber) {
  const generations = [...new Set(groups.map((g) => g.generationNumber))].sort((a, b) => b - a);
  const chosen = generationNumber ?? generations[0] ?? null;
  return {
    generationNumber: chosen,
    generations,
    maxAverageGap: MAX_AVERAGE_GAP,
    groups: groups
      .filter((g) => g.generationNumber === chosen)
      .sort((a, b) => a.groupNumber - b.groupNumber)
      .map((g) => ({
        id: g.id,
        number: g.groupNumber,
        average: g.average,
        gapToOthers: g.gapToOthers,
        members: g.members,
      })),
  };
}

/**
 * POST /api/group-contexts
 * @param {{ name: string, studentIds: number[] }} context
 */
export function createGroupContext(context) {
  return request('/group-contexts', json('POST', context), ERRORS);
}

/**
 * GET /api/group-contexts
 * @returns {Promise<Array>} contextos e números das gerações
 */
export function getGroupContexts() {
  return request('/group-contexts', {}, ERRORS);
}

/**
 * POST /api/group-contexts/{id}/generate
 * @returns {Promise<object>} a geração nova
 */
export async function generateGroups(contextId) {
  return toGeneration(await request(`/group-contexts/${contextId}/generate`, json('POST'), ERRORS));
}

/**
 * GET /api/group-contexts/{id}/groups
 * @param {number} contextId identificador do contexto
 * @param {number} [generationNumber] geração a abrir; por omissão a mais recente
 */
export async function getGroups(contextId, generationNumber) {
  return toGeneration(await request(`/group-contexts/${contextId}/groups`, {}, ERRORS), generationNumber);
}

/**
 * POST /api/group-contexts/{id}/swap (#75, backend na #112)
 * Troca dois alunos de grupos diferentes. Depois volta a pedir os grupos, para mostrar
 * as médias recalculadas pelo backend, seja qual for o corpo da resposta da troca.
 *
 * @param {number} contextId identificador do contexto
 * @param {number} studentIdA aluno de um grupo
 * @param {number} studentIdB aluno de outro grupo
 */
export async function swapStudents(contextId, studentIdA, studentIdB) {
  await request(`/group-contexts/${contextId}/swap`, json('POST', { studentIdA, studentIdB }), {
    ...ERRORS,
    404: 'A troca de alunos ainda não está disponível.',
    409: 'Esses alunos já estão no mesmo grupo.',
  });
  return getGroups(contextId);
}

// ---------------------------------------------------------------------------
// Área do docente: grupos de uma UC (#106). As respostas já vêm no formato dos ecrãs,
// com as estatísticas de cada grupo (R6) e os alunos sem grupo.
// ---------------------------------------------------------------------------

const TEACHER_ERRORS = {
  ...ERRORS,
  403: 'Só podes gerir grupos das tuas unidades curriculares.',
  404: 'Contexto ou grupo não encontrado nesta UC.',
};

/**
 * Pedidos dos grupos de uma UC do docente (/api/teacher/course-units/{id}/group-contexts).
 *
 * @param {number} courseUnitId UC do docente
 */
export function teacherGroupApi(courseUnitId) {
  const base = `/teacher/course-units/${courseUnitId}/group-contexts`;
  const groups = (contextId) => `${base}/${contextId}/groups`;
  return {
    list: () => request(base, {}, TEACHER_ERRORS),
    create: (context) => request(base, json('POST', context), TEACHER_ERRORS),
    generate: (contextId) => request(`${base}/${contextId}/generate`, json('POST'), TEACHER_ERRORS),
    groups: (contextId) => request(groups(contextId), {}, TEACHER_ERRORS),
    createGroup: (contextId, studentIds) =>
      request(groups(contextId), json('POST', { studentIds }), TEACHER_ERRORS),
    updateGroup: (contextId, groupId, studentIds) =>
      request(`${groups(contextId)}/${groupId}`, json('PUT', { studentIds }), TEACHER_ERRORS),
    deleteGroup: (contextId, groupId) =>
      request(`${groups(contextId)}/${groupId}`, { method: 'DELETE' }, TEACHER_ERRORS),
  };
}
