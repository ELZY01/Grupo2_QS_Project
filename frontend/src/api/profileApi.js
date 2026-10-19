import { request } from './http';

/**
 * GET /api/students/{studentId}/profile
 * @returns {Promise<object>} perfil de competências com a explicação de cada nível
 */
export function getProfile(studentId) {
  return request(`/students/${studentId}/profile`, {}, { 404: 'Estudante não encontrado.' });
}
