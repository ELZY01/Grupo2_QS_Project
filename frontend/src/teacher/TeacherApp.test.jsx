import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { HttpError } from '../api/http';
import TeacherApp from './TeacherApp.jsx';

vi.mock('../api/teacherApi', () => ({
  getMyCourseUnits: vi.fn(),
  getCourseUnitStudents: vi.fn(),
}));
// As estatísticas reutilizam os componentes do perfil e do histórico (testados à parte).
vi.mock('../components/CompetencyProfile', () => ({
  default: ({ studentId }) => <p>Perfil do aluno {studentId}</p>,
}));
vi.mock('../components/AcademicHistory', () => ({
  default: ({ studentId, readOnly, emptyText }) => (
    <p>Histórico do aluno {studentId} {readOnly ? '(só leitura)' : ''} {emptyText}</p>
  ),
}));

const api = await import('../api/teacherApi');

const QS = { courseUnit: { id: 10, code: 'QS', name: 'Qualidade de Software' }, studentCount: 2 };
const ES = { courseUnit: { id: 11, code: 'ES', name: 'Engenharia de Software' }, studentCount: 0 };
const ANA = { id: 7, name: 'Ana Costa', studentNumber: 'a22001', email: 'ana@ulusofona.pt', course: 'LEI', academicYear: 3 };

afterEach(() => {
  cleanup();
  vi.clearAllMocks();
});

describe('TeacherApp', () => {
  it('lists only the teacher units and opens the first one with its students', async () => {
    api.getMyCourseUnits.mockResolvedValue([QS, ES]);
    api.getCourseUnitStudents.mockResolvedValue([ANA]);
    render(<TeacherApp name="Prof. Rita" onLogout={vi.fn()} />);

    expect(await screen.findByRole('heading', { name: 'Qualidade de Software' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Engenharia de Software/ })).toBeInTheDocument();
    expect(await screen.findByText('Ana Costa')).toBeInTheDocument();
    expect(api.getCourseUnitStudents).toHaveBeenCalledWith(10);
  });

  it('shows the stats of the chosen student (profile and read-only history)', async () => {
    api.getMyCourseUnits.mockResolvedValue([QS]);
    api.getCourseUnitStudents.mockResolvedValue([ANA]);
    render(<TeacherApp name="Prof. Rita" onLogout={vi.fn()} />);

    fireEvent.click(await screen.findByRole('button', { name: /Ana Costa/ }));

    expect(screen.getByText('Perfil do aluno 7')).toBeInTheDocument();
    expect(screen.getByText(/Histórico do aluno 7 \(só leitura\)/)).toHaveTextContent(
      'Este aluno ainda não tem cadeiras registadas.',
    );
  });

  it('shows a clear empty state without units', async () => {
    api.getMyCourseUnits.mockResolvedValue([]);
    render(<TeacherApp name="Prof. Rita" onLogout={vi.fn()} />);

    expect(await screen.findByText('Ainda não tens unidades curriculares')).toBeInTheDocument();
    expect(api.getCourseUnitStudents).not.toHaveBeenCalled();
  });

  it('shows a clear empty state for a unit without students', async () => {
    api.getMyCourseUnits.mockResolvedValue([ES]);
    api.getCourseUnitStudents.mockResolvedValue([]);
    render(<TeacherApp name="Prof. Rita" onLogout={vi.fn()} />);

    expect(await screen.findByText(/Ainda não há alunos inscritos nesta UC/)).toBeInTheDocument();
  });

  it('shows the 403 message from the backend', async () => {
    api.getMyCourseUnits.mockResolvedValue([QS]);
    api.getCourseUnitStudents.mockRejectedValue(new HttpError(403, 'Esta UC não te está atribuída.'));
    render(<TeacherApp name="Prof. Rita" onLogout={vi.fn()} />);

    expect(await screen.findByRole('alert')).toHaveTextContent('Esta UC não te está atribuída.');
  });

  it('has a groups section with a clear empty state and a logout button', async () => {
    api.getMyCourseUnits.mockResolvedValue([QS]);
    api.getCourseUnitStudents.mockResolvedValue([ANA]);
    const onLogout = vi.fn();
    render(<TeacherApp name="Prof. Rita" onLogout={onLogout} />);

    fireEvent.click(await screen.findByRole('tab', { name: 'Grupos' }));
    expect(screen.getByText(/ficam disponíveis quando a formação de grupos/)).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'Sair' }));
    expect(onLogout).toHaveBeenCalled();
  });
});
