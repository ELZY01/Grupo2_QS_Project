import { cleanup, fireEvent, render, screen, within } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { HttpError } from '../api/http';
import CourseUnitAssignments from './CourseUnitAssignments.jsx';

vi.mock('../api/adminApi', () => ({
  getTeachers: vi.fn(),
  createTeacher: vi.fn(),
  getAssignments: vi.fn(),
  assignTeacher: vi.fn(),
  removeTeacher: vi.fn(),
  enrollStudent: vi.fn(),
  removeStudent: vi.fn(),
}));
vi.mock('../api/enrollmentApi', () => ({ getCourseUnits: vi.fn() }));
vi.mock('../api/studentApi', () => ({ getStudents: vi.fn() }));

const admin = await import('../api/adminApi');
const { getCourseUnits } = await import('../api/enrollmentApi');
const { getStudents } = await import('../api/studentApi');

const UNIT = { id: 10, code: 'QS', name: 'Qualidade de Software' };
const RITA = { id: 3, username: 'rita', displayName: 'Prof. Rita' };
const ANA = { id: 7, studentNumber: 'a22001', name: 'Ana Costa', email: 'ana@ulusofona.pt' };
const RUI = { id: 8, studentNumber: 'a22002', name: 'Rui Dias', email: 'rui@ulusofona.pt' };

const state = (teachers = [], students = []) => ({ courseUnit: UNIT, teachers, students });

beforeEach(() => {
  getCourseUnits.mockResolvedValue([UNIT]);
  admin.getTeachers.mockResolvedValue([RITA]);
  getStudents.mockResolvedValue([ANA, RUI]);
  admin.getAssignments.mockResolvedValue(state([], [ANA]));
});

afterEach(() => {
  cleanup();
  vi.clearAllMocks();
});

const pickUnit = async () => {
  render(<CourseUnitAssignments />);
  fireEvent.change(await screen.findByLabelText('Unidade curricular'), { target: { value: '10' } });
  return screen.findByText('Alunos inscritos (1)');
};

describe('CourseUnitAssignments', () => {
  it('shows the teachers and the enrolled students of the chosen unit', async () => {
    await pickUnit();
    expect(admin.getAssignments).toHaveBeenCalledWith('10');
    expect(screen.getByText('Ainda não há docentes nesta UC.')).toBeInTheDocument();
    expect(screen.getByText('Ana Costa')).toBeInTheDocument();
  });

  it('assigns a teacher to the unit (R2)', async () => {
    admin.assignTeacher.mockResolvedValue(state([RITA], [ANA]));
    await pickUnit();

    fireEvent.change(screen.getByLabelText('Associar docente'), { target: { value: '3' } });
    fireEvent.click(screen.getByRole('button', { name: 'Associar' }));

    expect(await screen.findByRole('button', { name: 'Remover Prof. Rita' })).toBeInTheDocument();
    expect(admin.assignTeacher).toHaveBeenCalledWith('10', 3);
  });

  it('only offers students that are not enrolled yet and enrolls them (R1)', async () => {
    admin.enrollStudent.mockResolvedValue(state([], [ANA, RUI]));
    await pickUnit();

    fireEvent.change(screen.getByLabelText('Inscrever aluno'), { target: { value: 'a2200' } });
    const found = screen.getByRole('list', { name: 'Alunos encontrados' });
    expect(within(found).queryByText('Ana Costa')).toBeNull();

    fireEvent.click(within(found).getByRole('button', { name: 'Inscrever Rui Dias' }));

    expect(await screen.findByText('Alunos inscritos (2)')).toBeInTheDocument();
    expect(admin.enrollStudent).toHaveBeenCalledWith('10', 8);
  });

  it('removes an enrolled student', async () => {
    admin.removeStudent.mockResolvedValue(state([], []));
    await pickUnit();

    fireEvent.click(screen.getByRole('button', { name: 'Remover Ana Costa' }));

    expect(await screen.findByText('Ainda não há alunos inscritos nesta UC.')).toBeInTheDocument();
    expect(admin.removeStudent).toHaveBeenCalledWith('10', 7);
  });

  it('shows the backend error, e.g. a duplicate enrollment', async () => {
    admin.enrollStudent.mockRejectedValue(new HttpError(409, 'Este aluno já está inscrito nesta UC.'));
    await pickUnit();

    fireEvent.change(screen.getByLabelText('Inscrever aluno'), { target: { value: 'rui' } });
    fireEvent.click(screen.getByRole('button', { name: 'Inscrever Rui Dias' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Este aluno já está inscrito nesta UC.');
  });

  it('creates a teacher account', async () => {
    admin.createTeacher.mockResolvedValue({ id: 4, username: 'joao', displayName: 'Prof. João' });
    render(<CourseUnitAssignments />);
    const form = await screen.findByRole('form', { name: 'Novo docente' });

    fireEvent.change(within(form).getByLabelText('Nome'), { target: { value: ' Prof. João ' } });
    fireEvent.change(within(form).getByLabelText('Utilizador'), { target: { value: 'joao' } });
    fireEvent.change(within(form).getByLabelText('Palavra-passe inicial'), { target: { value: 'segredo123' } });
    fireEvent.click(within(form).getByRole('button', { name: 'Criar docente' }));

    expect(await screen.findByRole('status')).toHaveTextContent('Docente Prof. João criado.');
    expect(admin.createTeacher).toHaveBeenCalledWith({
      displayName: 'Prof. João',
      username: 'joao',
      password: 'segredo123',
    });
  });
});
