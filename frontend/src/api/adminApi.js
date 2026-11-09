import { request } from './http';

const ERRORS = {
  400: 'Os dados são inválidos.',
  404: 'Não encontrado.',
  409: 'Já existe.',
};

const json = (method, body) => ({
  method,
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(body),
});

/** GET /api/admin/teachers */
export function getTeachers() {
  return request('/admin/teachers', {}, ERRORS);
}

/** POST /api/admin/teachers { username, displayName, password } */
export function createTeacher(teacher) {
  return request('/admin/teachers', json('POST', teacher), {
    ...ERRORS,
    409: 'Já existe uma conta com este utilizador.',
  });
}

/** GET /api/admin/course-units/{id}/assignments → { courseUnit, teachers, students } */
export function getAssignments(courseUnitId) {
  return request(`/admin/course-units/${courseUnitId}/assignments`, {}, ERRORS);
}

/** POST /api/admin/course-units/{id}/teachers { id } */
export function assignTeacher(courseUnitId, teacherId) {
  return request(`/admin/course-units/${courseUnitId}/teachers`, json('POST', { id: teacherId }), {
    ...ERRORS,
    409: 'Este docente já está associado a esta UC.',
  });
}

/** DELETE /api/admin/course-units/{id}/teachers/{teacherId} */
export function removeTeacher(courseUnitId, teacherId) {
  return request(`/admin/course-units/${courseUnitId}/teachers/${teacherId}`, { method: 'DELETE' }, ERRORS);
}

/** POST /api/admin/course-units/{id}/students { id } */
export function enrollStudent(courseUnitId, studentId) {
  return request(`/admin/course-units/${courseUnitId}/students`, json('POST', { id: studentId }), {
    ...ERRORS,
    409: 'Este aluno já está inscrito nesta UC.',
  });
}

/** DELETE /api/admin/course-units/{id}/students/{studentId} */
export function removeStudent(courseUnitId, studentId) {
  return request(`/admin/course-units/${courseUnitId}/students/${studentId}`, { method: 'DELETE' }, ERRORS);
}
