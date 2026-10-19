import { request } from './http';

const ENROLLMENT_ERRORS = {
  400: 'Os dados da inscrição são inválidos.',
  404: 'Estudante ou unidade curricular não encontrados.',
};

/**
 * GET /api/course-units
 * @returns {Promise<Array>} unidades curriculares disponíveis
 */
export function getCourseUnits() {
  return request('/course-units');
}

/**
 * GET /api/students/{studentId}/enrollments
 * @returns {Promise<Array>} inscrições (tentativas) do estudante
 */
export function getEnrollments(studentId) {
  return request(`/students/${studentId}/enrollments`, {}, ENROLLMENT_ERRORS);
}

/**
 * POST /api/students/{studentId}/enrollments
 * @param {number} studentId identificador do estudante
 * @param {object} enrollment { courseUnitId, schoolYear, semester, grade, status }
 */
export function createEnrollment(studentId, enrollment) {
  return request(
    `/students/${studentId}/enrollments`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(enrollment),
    },
    ENROLLMENT_ERRORS
  );
}
