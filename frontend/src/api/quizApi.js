import { API_URL, apiFetch } from './http';

const API_BASE_URL = `${API_URL}/api`;

// Obter as 20 perguntas do quiz agrupadas/associadas
export const fetchQuiz = async () => {
    const response = await apiFetch(`${API_BASE_URL}/quiz`);
    if (!response.ok) {
        throw new Error('Não foi possível carregar o questionário.');
    }
    return response.json();
};

// Submeter as respostas do estudante
export const submitQuizSubmission = async (studentId, quizId, answers) => {
    const response = await apiFetch(`${API_BASE_URL}/students/${studentId}/quiz-submissions`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            quizId: quizId,
            answers: answers
        }),
    });

    if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || 'Erro ao submeter as respostas do quiz.');
    }

    return response.json();
};