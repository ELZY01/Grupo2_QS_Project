import { afterEach, describe, expect, it, vi } from 'vitest';
import { fetchQuiz, submitQuizSubmission } from './quizApi.js';

const reply = (status, body) =>
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
        ok: status < 400,
        status,
        json: () => Promise.resolve(body),
    });

afterEach(() => vi.restoreAllMocks());

describe('quizApi', () => {
    it('uses the same API address as the other files', async () => {
        const fetchMock = reply(200, []);
        await fetchQuiz();
        expect(fetchMock).toHaveBeenCalledWith('http://localhost:8080/api/quiz', {});
    });

    it('sends the answers of the student', async () => {
        const fetchMock = reply(201, { id: 1 });
        await submitQuizSubmission(3, 1, [{ questionId: 1, value: 4 }]);

        const [url, options] = fetchMock.mock.calls[0];
        expect(url).toBe('http://localhost:8080/api/students/3/quiz-submissions');
        expect(options.method).toBe('POST');
        expect(JSON.parse(options.body)).toEqual({ quizId: 1, answers: [{ questionId: 1, value: 4 }] });
    });

    it('shows the message sent by the API', async () => {
        reply(400, { message: 'O quiz tem de ter 20 respostas.' });
        await expect(submitQuizSubmission(3, 1, [])).rejects.toThrow('O quiz tem de ter 20 respostas.');
    });

    it('uses a clear message when the API does not send one', async () => {
        reply(404, {});
        await expect(fetchQuiz()).rejects.toThrow('Estudante ou quiz não encontrado.');
    });
});