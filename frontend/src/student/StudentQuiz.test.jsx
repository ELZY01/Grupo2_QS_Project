import { act, cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import StudentQuiz from './StudentQuiz.jsx';

const QUESTIONS = [
  { id: 1, text: 'Planeio a semana', competency: { name: 'Organização' } },
  { id: 2, text: 'Entrego a horas', competency: { name: 'Gestão de tempo' } },
];

const json = (body) => Promise.resolve({ ok: true, status: 200, json: () => Promise.resolve(body) });

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
  vi.useRealTimers();
});

describe('StudentQuiz', () => {
  it('shows one question at a time and cracks the egg as the student answers', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation(() => json(QUESTIONS));
    render(<StudentQuiz studentId={3} onDone={() => {}} />);

    expect(await screen.findByText('Planeio a semana')).toBeInTheDocument();
    expect(screen.queryByText('Entrego a horas')).not.toBeInTheDocument();
    expect(screen.getByText('0 de 2 respondidas')).toBeInTheDocument();

    vi.useFakeTimers();
    fireEvent.click(screen.getByRole('radio', { name: /Bastante/ }));
    act(() => vi.advanceTimersByTime(300));

    expect(screen.getByText('1 de 2 respondidas')).toBeInTheDocument();
    expect(screen.getByText('Entrego a horas')).toBeInTheDocument();
    expect(screen.getByRole('img', { name: /O ovo está a rachar: 1 de 2/ })).toBeInTheDocument();
  });

  it('only lets the egg hatch when every question is answered', async () => {
    const onDone = vi.fn();
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockImplementation(() => json(QUESTIONS));
    render(<StudentQuiz studentId={3} onDone={onDone} />);
    await screen.findByText('Planeio a semana');

    fireEvent.click(screen.getByRole('radio', { name: /Bastante/ }));
    fireEvent.click(await screen.findByRole('button', { name: 'Seguinte' }).catch(() => screen.getByText('Seguinte')));
    const hatch = await screen.findByRole('button', { name: 'Chocar o ovo!' });
    expect(hatch).toBeDisabled();

    fireEvent.click(screen.getByRole('radio', { name: /Sou mesmo eu/ }));
    expect(hatch).toBeEnabled();

    fireEvent.click(hatch);
    await vi.waitFor(() => expect(onDone).toHaveBeenCalled());
    const [url, options] = fetchMock.mock.calls.at(-1);
    expect(url).toContain('/students/3/quiz-submissions');
    expect(JSON.parse(options.body).answers).toHaveLength(2);
  });
});
