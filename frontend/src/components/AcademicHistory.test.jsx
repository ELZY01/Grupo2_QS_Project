import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import AcademicHistory from './AcademicHistory';

const UNITS = [
  { id: 1, code: 'PROG1', name: 'Programação I', ects: 6, academicYear: 1, semester: 1 },
  { id: 2, code: 'BD', name: 'Bases de Dados', ects: 6, academicYear: 2, semester: 1 },
];

const HISTORY = [
  { id: 11, studentId: 7, courseUnitId: 2, schoolYear: 2025, semester: 1, attemptNumber: 1, grade: 14, status: 'APROVADO' },
  { id: 10, studentId: 7, courseUnitId: 1, schoolYear: 2024, semester: 1, attemptNumber: 1, grade: 8, status: 'REPROVADO' },
];

const jsonResponse = (status, body) => ({
  ok: status >= 200 && status < 300,
  status,
  json: async () => body,
});

// Responde aos GET pelo URL; o POST devolve `postResponse`.
const mockApi = (postResponse) =>
  vi.spyOn(globalThis, 'fetch').mockImplementation(async (url, options = {}) => {
    if (options.method === 'POST') return postResponse;
    if (url.endsWith('/api/course-units')) return jsonResponse(200, UNITS);
    if (url.endsWith('/api/students/7/enrollments')) return jsonResponse(200, HISTORY);
    return jsonResponse(404, {});
  });

describe('AcademicHistory', () => {
  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
  });

  it('shows every attempt with UC name, grade and status, ordered by school year', async () => {
    mockApi();
    render(<AcademicHistory studentId={7} />);

    await screen.findByRole('table');
    const rows = screen.getAllByRole('row');
    expect(rows[1]).toHaveTextContent('2024/2025');
    expect(rows[1]).toHaveTextContent('PROG1 · Programação I');
    expect(rows[1]).toHaveTextContent('Reprovado');
    expect(rows[2]).toHaveTextContent('BD · Bases de Dados');
    expect(rows[2]).toHaveTextContent('Aprovado');
  });

  it('registers an enrollment with POST and notifies the parent', async () => {
    const created = { id: 12, studentId: 7, courseUnitId: 1, schoolYear: 2025, semester: 1, attemptNumber: 2, grade: 15, status: 'APROVADO' };
    mockApi(jsonResponse(201, created));
    const onChange = vi.fn();
    render(<AcademicHistory studentId={7} onChange={onChange} />);
    await screen.findByRole('table');

    fireEvent.change(screen.getByLabelText('Unidade curricular'), { target: { value: '1' } });
    fireEvent.change(screen.getByLabelText('Ano letivo (início)'), { target: { value: '2025' } });
    fireEvent.change(screen.getByLabelText('Nota (0–20)'), { target: { value: '15' } });
    fireEvent.click(screen.getByRole('button', { name: 'Registar inscrição' }));

    expect(await screen.findByRole('status')).toHaveTextContent('Programação I, tentativa 2');
    const [url, options] = globalThis.fetch.mock.calls.find(([, o]) => o?.method === 'POST');
    expect(url).toBe('http://localhost:8080/api/students/7/enrollments');
    expect(JSON.parse(options.body)).toEqual({
      courseUnitId: 1,
      schoolYear: 2025,
      semester: 1,
      status: 'APROVADO',
      grade: 15,
    });
    expect(onChange).toHaveBeenCalledTimes(1);
  });

  it('shows the validation error returned by the API', async () => {
    mockApi(jsonResponse(400, { message: 'A nota deve estar entre 0 e 20.' }));
    render(<AcademicHistory studentId={7} />);
    await screen.findByRole('table');

    fireEvent.change(screen.getByLabelText('Unidade curricular'), { target: { value: '1' } });
    fireEvent.change(screen.getByLabelText('Nota (0–20)'), { target: { value: '25' } });
    fireEvent.click(screen.getByRole('button', { name: 'Registar inscrição' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('A nota deve estar entre 0 e 20.');
  });

  it('sends no grade for an enrollment in progress', async () => {
    const created = { id: 13, studentId: 7, courseUnitId: 2, schoolYear: 2026, semester: 1, attemptNumber: 2, grade: null, status: 'EM_CURSO' };
    mockApi(jsonResponse(201, created));
    render(<AcademicHistory studentId={7} />);
    await screen.findByRole('table');

    fireEvent.change(screen.getByLabelText('Unidade curricular'), { target: { value: '2' } });
    fireEvent.change(screen.getByLabelText('Estado'), { target: { value: 'EM_CURSO' } });
    expect(screen.getByLabelText('Nota (0–20)')).toBeDisabled();
    fireEvent.click(screen.getByRole('button', { name: 'Registar inscrição' }));

    await screen.findByRole('status');
    const [, options] = globalThis.fetch.mock.calls.find(([, o]) => o?.method === 'POST');
    expect(JSON.parse(options.body).grade).toBeNull();
    expect(screen.getAllByRole('row').at(-1)).toHaveTextContent('Em curso');
  });

  it('shows an error when the history cannot be loaded', async () => {
    vi.spyOn(globalThis, 'fetch').mockRejectedValue(new Error('Failed to fetch'));
    render(<AcademicHistory studentId={7} />);

    expect(await screen.findByRole('alert')).toHaveTextContent('Não foi possível carregar o histórico');
  });
});
