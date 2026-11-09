import { act, cleanup, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { HttpError } from '../api/http';
import GroupContexts from './GroupContexts.jsx';
import GroupResults from './GroupResults.jsx';

vi.mock('../api/groupApi', () => ({
  createGroupContext: vi.fn(),
  getGroupContexts: vi.fn(),
  generateGroups: vi.fn(),
  getGroups: vi.fn(),
}));
vi.mock('../api/studentApi', () => ({ getStudents: vi.fn() }));

const groupApi = await import('../api/groupApi');
const { getStudents } = await import('../api/studentApi');

const STUDENTS = Array.from({ length: 8 }, (_, i) => ({
  id: i + 1,
  name: `Aluno ${i + 1}`,
  studentNumber: `a${1000 + i}`,
}));

const member = (id, extra = {}) => ({ studentId: id, name: `Aluno ${id}`, studentNumber: `a${999 + id}`, ...extra });

const GENERATION = {
  contextId: 7,
  contextName: 'Projeto final',
  generatedAt: '2026-11-08T10:00:00Z',
  maxAverageGap: 0.5,
  groups: [
    {
      number: 1,
      average: 3.46,
      gapToOthers: 0.12,
      // Mesmo que a API mande notas, o ecrã não as pode mostrar (RN26).
      members: [member(1, { average: 17.3, level: 4.8 }), member(4), member(5), member(8)],
      strongestByArea: [
        { area: 'ORG', studentName: 'Aluno 1' },
        { area: 'TEMPO', studentName: 'Aluno 4' },
        { area: 'PROG', studentName: 'Aluno 5' },
      ],
    },
    {
      number: 2,
      average: 3.34,
      gapToOthers: -0.12,
      members: [member(2), member(3), member(6), member(7)],
      strongestByArea: [{ area: 'EQUIPA', studentName: 'Aluno 6' }],
    },
  ],
};

beforeEach(() => {
  getStudents.mockResolvedValue(STUDENTS);
  groupApi.getGroupContexts.mockResolvedValue([]);
});

afterEach(() => {
  cleanup();
  vi.clearAllMocks();
  vi.useRealTimers();
});

describe('GroupContexts', () => {
  it('creates a context with the chosen students and shows the group split', async () => {
    groupApi.createGroupContext.mockResolvedValue({
      id: 7, name: 'Projeto final', createdAt: '2026-11-08T09:00:00Z', studentCount: 8, generatedAt: null,
    });
    render(<GroupContexts />);

    fireEvent.change(screen.getByLabelText('Nome da atividade'), { target: { value: ' Projeto final ' } });
    fireEvent.click(await screen.findByLabelText(/Todos \(8\)/));
    expect(screen.getByText('8 alunos → 2 grupos (4 + 4)')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'Criar contexto' }));

    await waitFor(() => expect(groupApi.createGroupContext).toHaveBeenCalledWith({
      name: 'Projeto final',
      studentIds: [1, 2, 3, 4, 5, 6, 7, 8],
    }));
    expect(await screen.findByRole('heading', { name: 'Projeto final' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Gerar grupos' })).toBeInTheDocument();
  });

  it('blocks an impossible configuration with a clear message', async () => {
    render(<GroupContexts />);
    fireEvent.change(screen.getByLabelText('Nome da atividade'), { target: { value: 'Mini projeto' } });
    for (const n of [1, 2, 3, 4, 5, 6, 7]) {
      fireEvent.click(await screen.findByRole('checkbox', { name: new RegExp(`Aluno ${n}\\b`) }));
    }

    expect(screen.getByText('Com 7 alunos não dá para fazer grupos de 4 ou 5.')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Criar contexto' }));

    expect(screen.getByRole('alert')).toHaveTextContent('Com 7 alunos não dá para fazer grupos de 4 ou 5.');
    expect(groupApi.createGroupContext).not.toHaveBeenCalled();
  });

  it('shows the API error when the backend rejects the generation', async () => {
    groupApi.getGroupContexts.mockResolvedValue([
      { id: 3, name: 'Trabalho 1', studentCount: 6, generatedAt: null },
    ]);
    groupApi.generateGroups.mockRejectedValue(
      new HttpError(400, 'Com 6 alunos não é possível formar grupos de 4 ou 5.'),
    );
    render(<GroupContexts />);

    fireEvent.click(await screen.findByRole('button', { name: /Trabalho 1/ }));
    fireEvent.click(screen.getByRole('button', { name: 'Gerar grupos' }));

    expect(await screen.findByRole('alert', {}, { timeout: 3000 }))
      .toHaveTextContent('Com 6 alunos não é possível formar grupos de 4 ou 5.');
  });

  it('shows the Pix animation for at least 2 s while generating', async () => {
    groupApi.getGroupContexts.mockResolvedValue([
      { id: 7, name: 'Projeto final', studentCount: 8, generatedAt: null },
    ]);
    groupApi.generateGroups.mockResolvedValue(GENERATION);
    render(<GroupContexts />);
    fireEvent.click(await screen.findByRole('button', { name: /Projeto final/ }));

    vi.useFakeTimers();
    await act(async () => {
      fireEvent.click(screen.getByRole('button', { name: 'Gerar grupos' }));
    });

    expect(screen.getByText('A formar grupos…')).toBeInTheDocument();
    await act(async () => vi.advanceTimersByTime(1500));
    expect(screen.getByText('A formar grupos…')).toBeInTheDocument();

    await act(async () => vi.advanceTimersByTime(600));
    expect(screen.queryByText('A formar grupos…')).toBeNull();
    expect(screen.getByRole('article', { name: 'Grupo 1' })).toBeInTheDocument();
  });

  it('opens a previous generation from the history', async () => {
    groupApi.getGroupContexts.mockResolvedValue([
      { id: 7, name: 'Projeto final', studentCount: 8, generatedAt: '2026-11-08T10:00:00Z' },
    ]);
    groupApi.getGroups.mockResolvedValue(GENERATION);
    render(<GroupContexts />);

    fireEvent.click(await screen.findByRole('button', { name: /Projeto final/ }));

    expect(await screen.findByRole('article', { name: 'Grupo 2' })).toBeInTheDocument();
    expect(groupApi.getGroups).toHaveBeenCalledWith(7);
    expect(screen.getByRole('button', { name: 'Gerar de novo' })).toBeInTheDocument();
  });
});

describe('GroupResults', () => {
  it('shows members, average, strongest per area and the gap to the other groups', () => {
    render(<GroupResults generation={GENERATION} />);
    const g1 = screen.getByRole('article', { name: 'Grupo 1' });

    expect(within(g1).getByText('média 3,5 / 5')).toBeInTheDocument();
    expect(within(g1).getByText('+0,1 em relação à média dos outros grupos')).toBeInTheDocument();
    expect(within(g1).getAllByRole('listitem').map((li) => li.textContent)).toEqual(
      expect.arrayContaining(['Aluno 1a1000', 'OrganizaçãoAluno 1', 'ProgramaçãoAluno 5']),
    );
    expect(within(g1).getByText('Trabalho em grupo').nextSibling).toHaveTextContent('sem dados');
    expect(screen.getByText(/equilibrado/)).toBeInTheDocument();
  });

  it('never shows the grades or levels of other students (RN26)', () => {
    render(<GroupResults generation={GENERATION} />);
    const text = document.body.textContent;
    expect(text).not.toMatch(/17[,.]3/);
    expect(text).not.toMatch(/4[,.]8/);
  });

  it('warns when the gap between groups is above the limit', () => {
    const unbalanced = {
      ...GENERATION,
      groups: [{ ...GENERATION.groups[0], average: 4.2 }, { ...GENERATION.groups[1], average: 3.1 }],
    };
    render(<GroupResults generation={unbalanced} />);
    expect(screen.getByText(/acima do limite/)).toBeInTheDocument();
  });
});
