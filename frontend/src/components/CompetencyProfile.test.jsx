import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import CompetencyProfile from './CompetencyProfile';

const PROFILE = {
  studentId: 7,
  studentName: 'Ana Silva',
  completedCourseUnits: 3,
  quizWeight: 0.5,
  quizCompleted: true,
  competencies: [
    {
      code: 'PROG',
      name: 'Programação',
      level: 3.5,
      origin: 'MIXED',
      quizWeight: 0.5,
      quizLevel: 4.0,
      historyLevel: 3.0,
      contributions: [
        { courseUnitCode: 'PROG1', courseUnitName: 'Programação I', grade: 14, gradeLevel: 3.8, weight: 1.0, share: 0.6 },
        { courseUnitCode: 'BD', courseUnitName: 'Bases de Dados', grade: 12, gradeLevel: 3.4, weight: 0.8, share: 0.4 },
      ],
    },
    {
      code: 'EQUIPA',
      name: 'Trabalho em grupo',
      level: null,
      origin: 'NO_DATA',
      quizWeight: 0,
      quizLevel: null,
      historyLevel: null,
      contributions: [],
    },
  ],
};

const jsonResponse = (status, body) => ({
  ok: status >= 200 && status < 300,
  status,
  json: async () => body,
});

describe('CompetencyProfile', () => {
  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
  });

  it('shows each competency with its level, origin and the quiz weight', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse(200, PROFILE));
    render(<CompetencyProfile studentId={7} />);

    expect(await screen.findByText('Programação')).toBeInTheDocument();
    expect(screen.getByText('3,5 / 5')).toBeInTheDocument();
    expect(screen.getByText('Misto')).toBeInTheDocument();
    expect(screen.getByText('50%')).toBeInTheDocument();
    expect(globalThis.fetch).toHaveBeenCalledWith('http://localhost:8080/api/students/7/profile', {});
  });

  it('explains a level with the UCs, grades, weights and shares', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse(200, PROFILE));
    render(<CompetencyProfile studentId={7} />);

    fireEvent.click(await screen.findByText('Programação'));

    const row = screen.getByText('PROG1 · Programação I').closest('tr');
    expect(row).toHaveTextContent('14');
    expect(row).toHaveTextContent('3,8');
    expect(row).toHaveTextContent('60%');
    expect(screen.getByText('4,0 · conta 50%')).toBeInTheDocument();
  });

  it('shows "Sem dados" with a suggested action', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse(200, PROFILE));
    render(<CompetencyProfile studentId={7} />);

    fireEvent.click(await screen.findByText('Trabalho em grupo'));

    expect(screen.getByText('Sem dados')).toBeInTheDocument();
    expect(screen.getByText(/faz o quiz inicial ou regista notas/)).toBeInTheDocument();
  });

  it('asks for the initial quiz when it is missing', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse(200, { ...PROFILE, quizCompleted: false }));
    render(<CompetencyProfile studentId={7} />);

    expect(await screen.findByText(/Ainda não respondeste ao quiz inicial/)).toBeInTheDocument();
  });

  it('reloads the profile when refreshKey changes', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse(200, PROFILE));
    const { rerender } = render(<CompetencyProfile studentId={7} refreshKey={0} />);
    await screen.findByText('Programação');

    rerender(<CompetencyProfile studentId={7} refreshKey={1} />);

    await vi.waitFor(() => expect(globalThis.fetch).toHaveBeenCalledTimes(2));
  });

  it('shows the API error when the student does not exist', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse(404, { message: 'Estudante não encontrado.' }));
    render(<CompetencyProfile studentId={99} />);

    expect(await screen.findByRole('alert')).toHaveTextContent('Estudante não encontrado.');
  });
});
