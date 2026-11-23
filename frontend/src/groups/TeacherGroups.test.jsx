import { cleanup, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { HttpError } from '../api/http';
import GroupContexts from './GroupContexts.jsx';

// Grupos de uma UC na área do docente (#106): o ecrã recebe os pedidos da UC (teacherGroupApi).

const student = (id) => ({ studentId: id, name: `Aluno ${id}`, studentNumber: `a${100 + id}` });

const area = (competencyCode, average, strongMembers, distribution) => ({
  competencyCode,
  average,
  min: average == null ? null : 2,
  max: average == null ? null : 4.5,
  membersWithData: average == null ? 0 : 4,
  strongMembers,
  distribution,
});

const groupView = (id, number, ids, extra = {}) => ({
  id,
  number,
  memberCount: ids.length,
  average: 3.2,
  gapToOthers: 0,
  balanced: true,
  gaps: [],
  areas: [
    area('ORG', 3.5, 2, { 1: 0, 2: 1, 3: 1, 4: 1, 5: 1 }),
    area('PROG', null, 0, { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 }),
  ],
  members: ids.map(student),
  strongestByArea: [{ area: 'ORG', studentName: `Aluno ${ids[0]}` }],
  ...extra,
});

const groups = (list, unassigned = []) => ({
  contextId: 20,
  contextName: 'Projeto final',
  generationNumber: list.length ? 1 : null,
  maxAverageGap: 0.5,
  averageSpread: 0,
  balanced: true,
  groups: list,
  unassigned: unassigned.map(student),
});

const CONTEXT = { id: 20, name: 'Projeto final', studentCount: 9, generatedAt: null, generationNumber: null };

let api;

beforeEach(() => {
  api = {
    students: vi.fn().mockResolvedValue([]),
    list: vi.fn().mockResolvedValue([CONTEXT]),
    create: vi.fn(),
    generate: vi.fn(),
    groups: vi.fn(),
    createGroup: vi.fn(),
    updateGroup: vi.fn(),
    deleteGroup: vi.fn(),
  };
});

afterEach(() => {
  cleanup();
  vi.clearAllMocks();
});

const open = async () => {
  render(<GroupContexts api={api} manage />);
  fireEvent.click(await screen.findByRole('button', { name: /Projeto final/ }));
};

const editor = () => screen.getByRole('form', { name: 'Editar grupos' });

const pick = (value) => fireEvent.change(within(editor()).getByLabelText('Grupo a alterar'), { target: { value } });

describe('teacher groups (#106)', () => {
  it('opens a context without groups and creates a group with the unassigned students', async () => {
    api.groups.mockResolvedValue(groups([], [1, 2, 3, 4, 5]));
    api.createGroup.mockResolvedValue(groups([groupView(31, 1, [1, 2, 3, 4])], [5]));
    await open();

    expect(await screen.findByText(/Ainda não há grupos/)).toBeInTheDocument();
    expect(screen.getByText('Sem grupo (5): Aluno 1, Aluno 2, Aluno 3, Aluno 4, Aluno 5')).toBeInTheDocument();

    pick('new');
    fireEvent.click(within(editor()).getByLabelText(/Aluno 1/));
    fireEvent.click(within(editor()).getByLabelText(/Aluno 2/));
    fireEvent.click(within(editor()).getByLabelText(/Aluno 3/));
    fireEvent.click(within(editor()).getByLabelText(/Aluno 4/));
    fireEvent.click(within(editor()).getByRole('button', { name: 'Criar grupo' }));

    await waitFor(() => expect(api.createGroup).toHaveBeenCalledWith(20, [1, 2, 3, 4]));
    expect(await screen.findByRole('article', { name: 'Grupo 1' })).toBeInTheDocument();
    expect(screen.getByText('Sem grupo (1): Aluno 5')).toBeInTheDocument();
    expect(api.groups).toHaveBeenCalledWith(20);
  });

  it('does not offer a new group without 4 students without a group', async () => {
    api.groups.mockResolvedValue(groups([groupView(31, 1, [1, 2, 3, 4, 5])], [6]));
    await open();
    await screen.findByRole('article', { name: 'Grupo 1' });

    expect(within(editor()).getByRole('option', { name: /Novo grupo/ })).toBeDisabled();
  });

  it('shows the statistics of each group (R6)', async () => {
    api.groups.mockResolvedValue(groups([
      groupView(31, 1, [1, 2, 3, 4], { gaps: ['PROG'], balanced: false }),
    ]));
    await open();

    const g1 = await screen.findByRole('article', { name: 'Grupo 1' });
    expect(within(g1).getByText('desequilibrado')).toBeInTheDocument();
    expect(within(g1).getByText('Lacunas (ninguém forte): Programação')).toBeInTheDocument();
    const org = within(g1).getByRole('row', { name: /Organização/ });
    expect(within(org).getAllByRole('cell').map((c) => c.textContent)).toEqual(['3,5', '2,0–4,5', '2', '0 · 1 · 1 · 1 · 1']);
    const prog = within(g1).getByRole('row', { name: /Programação/ });
    expect(within(prog).getAllByRole('cell')[0]).toHaveTextContent('sem dados');
    expect(within(g1).getByText('Aluno 1', { selector: '.group-area span' })).toBeInTheDocument();
  });

  it('removes a student from a group and the student becomes unassigned', async () => {
    api.groups.mockResolvedValue(groups([groupView(31, 1, [1, 2, 3, 4, 5]), groupView(32, 2, [6, 7, 8, 9])]));
    api.updateGroup.mockResolvedValue(groups([groupView(31, 1, [1, 2, 3, 4]), groupView(32, 2, [6, 7, 8, 9])], [5]));
    await open();
    await screen.findByRole('article', { name: 'Grupo 2' });

    pick('31');
    fireEvent.click(within(editor()).getByLabelText(/Aluno 5/));
    fireEvent.click(within(editor()).getByRole('button', { name: 'Guardar membros' }));

    await waitFor(() => expect(api.updateGroup).toHaveBeenCalledWith(20, 31, [1, 2, 3, 4]));
    expect(await screen.findByText('Sem grupo (1): Aluno 5')).toBeInTheDocument();
  });

  it('checks the size before calling the backend', async () => {
    api.groups.mockResolvedValue(groups([groupView(31, 1, [1, 2, 3, 4])]));
    await open();
    await screen.findByRole('article', { name: 'Grupo 1' });

    pick('31');
    fireEvent.click(within(editor()).getByLabelText(/Aluno 4/));
    fireEvent.click(within(editor()).getByRole('button', { name: 'Guardar membros' }));

    expect(within(editor()).getByRole('alert'))
      .toHaveTextContent('Um grupo tem de ter entre 4 e 5 alunos (escolheste 3).');
    expect(api.updateGroup).not.toHaveBeenCalled();
  });

  it('shows the 409 message when the backend rejects the change', async () => {
    api.groups.mockResolvedValue(groups([groupView(31, 1, [1, 2, 3, 4])], [5]));
    api.updateGroup.mockRejectedValue(new HttpError(409, 'O aluno 5 não está inscrito nesta UC.'));
    await open();
    await screen.findByRole('article', { name: 'Grupo 1' });

    pick('31');
    fireEvent.click(within(editor()).getByLabelText(/Aluno 5/));
    fireEvent.click(within(editor()).getByRole('button', { name: 'Guardar membros' }));

    expect(await within(editor()).findByRole('alert')).toHaveTextContent('O aluno 5 não está inscrito nesta UC.');
  });

  it('deletes a group', async () => {
    api.groups.mockResolvedValue(groups([groupView(31, 1, [1, 2, 3, 4]), groupView(32, 2, [5, 6, 7, 8])]));
    api.deleteGroup.mockResolvedValue(groups([groupView(31, 1, [1, 2, 3, 4])], [5, 6, 7, 8]));
    await open();
    await screen.findByRole('article', { name: 'Grupo 2' });

    pick('32');
    fireEvent.click(within(editor()).getByRole('button', { name: 'Apagar grupo 2' }));

    await waitFor(() => expect(api.deleteGroup).toHaveBeenCalledWith(20, 32));
    await waitFor(() => expect(screen.queryByRole('article', { name: 'Grupo 2' })).toBeNull());
  });

  it('generates the groups of the unit and has no swap panel', async () => {
    api.groups.mockResolvedValue(groups([], [1, 2, 3, 4, 5, 6, 7, 8, 9]));
    api.generate.mockResolvedValue(groups([groupView(31, 1, [1, 2, 3, 4, 5]), groupView(32, 2, [6, 7, 8, 9])]));
    await open();

    fireEvent.click(await screen.findByRole('button', { name: 'Gerar grupos' }));

    expect(await screen.findByRole('article', { name: 'Grupo 2' }, { timeout: 3000 })).toBeInTheDocument();
    expect(api.generate).toHaveBeenCalledWith(20);
    expect(screen.queryByRole('form', { name: 'Trocar alunos' })).toBeNull();
  });
});
