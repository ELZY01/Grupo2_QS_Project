import { request } from './http';

// Mensagens usadas quando a API não envia o texto do erro.
const QUIZ_ERRORS = {
    400: 'As respostas do quiz são inválidas. Responde às 20 perguntas.',
    404: 'Estudante ou quiz não encontrado.',
};

/**
 * GET /api/quiz
 * @returns {Promise<Array>} as 20 perguntas do quiz inicial
 */
export function fetchQuiz() {
    return request('/quiz', {}, QUIZ_ERRORS);
}

/**
 * POST /api/students/{studentId}/quiz-submissions
 * @param {number} studentId identificador do estudante
 * @param {number} quizId identificador do quiz
 * @param {Array<{questionId: number, value: number}>} answers respostas de 1 a 5
 */
export function submitQuizSubmission(studentId, quizId, answers) {
    return request(
        `/students/${studentId}/quiz-submissions`,
        {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ quizId, answers }),
        },
        QUIZ_ERRORS
    );
}