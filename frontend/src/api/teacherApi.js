import { request } from './http';

const ERRORS = {
  403: 'Esta UC não te está atribuída.',
  404: 'Unidade curricular não encontrada.',
};

/**
 * GET /api/teacher/course-units
 * @returns {Promise<Array<{courseUnit, studentCount}>>} as UCs do docente
 */
export function getMyCourseUnits() {
  return request('/teacher/course-units', {}, ERRORS);
}

/**
 * GET /api/teacher/course-units/{id}/students
 * @returns {Promise<Array>} alunos inscritos (sem notas)
 */
export function getCourseUnitStudents(courseUnitId) {
  return request(`/teacher/course-units/${courseUnitId}/students`, {}, ERRORS);
}
