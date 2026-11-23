import { afterEach, describe, expect, it, vi } from 'vitest';
import { generateGroups, getGroups, swapStudents, teacherGroupApi, toGeneration } from './groupApi.js';

const API = 'http://localhost:8080/api';

const reply = (...bodies) => {
  const fetchMock = vi.spyOn(globalThis, 'fetch');
  for (const [status, body] of bodies) {
    fetchMock.mockResolvedValueOnce({ ok: status < 400, status, json: () => Promise.resolve(body) });
  }
  return fetchMock;
};

// Formato do backend do #69: os grupos de todas as gerações numa lista só.
const BACKEND_GROUPS = [
  { id: 12, generationNumber: 2, groupNumber: 2, average: 3.1, gapToOthers: -0.2, members: [] },
  { id: 11, generationNumber: 2, groupNumber: 1, average: 3.3, gapToOthers: 0.2, members: [] },
  { id: 2, generationNumber: 1, groupNumber: 1, average: 3.0, gapToOthers: 0, members: [] },
];

afterEach(() => vi.restoreAllMocks());

describe('toGeneration', () => {
  it('shows the latest generation by default, with the groups in order', () => {
    const generation = toGeneration(BACKEND_GROUPS);

    expect(generation.generationNumber).toBe(2);
    expect(generation.generations).toEqual([2, 1]);
    expect(generation.maxAverageGap).toBe(0.5);
    expect(generation.groups.map((g) => [g.id, g.number])).toEqual([[11, 1], [12, 2]]);
  });

  it('opens an older generation', () => {
    const generation = toGeneration(BACKEND_GROUPS, 1);

    expect(generation.generationNumber).toBe(1);
    expect(generation.groups).toHaveLength(1);
  });

  it('handles a context without groups', () => {
    expect(toGeneration([])).toEqual({ generationNumber: null, generations: [], maxAverageGap: 0.5, groups: [] });
  });
});

describe('admin endpoints (#69)', () => {
  it('converts the groups of a context into one generation', async () => {
    const fetchMock = reply([200, BACKEND_GROUPS]);

    const generation = await getGroups(7, 1);

    expect(fetchMock).toHaveBeenCalledWith(`${API}/group-contexts/7/groups`, {});
    expect(generation.generationNumber).toBe(1);
  });

  it('converts the new generation', async () => {
    reply([200, BACKEND_GROUPS.slice(0, 2)]);

    const generation = await generateGroups(7);

    expect(generation.groups.map((g) => g.number)).toEqual([1, 2]);
  });

  it('reloads the groups after a swap to show the new averages', async () => {
    const fetchMock = reply([200, { ok: true }], [200, BACKEND_GROUPS]);

    const generation = await swapStudents(7, 1, 2);

    const [url, options] = fetchMock.mock.calls[0];
    expect(url).toBe(`${API}/group-contexts/7/swap`);
    expect(JSON.parse(options.body)).toEqual({ studentIdA: 1, studentIdB: 2 });
    expect(fetchMock.mock.calls[1][0]).toBe(`${API}/group-contexts/7/groups`);
    expect(generation.generationNumber).toBe(2);
  });

  it('explains that the swap is not available yet when the endpoint does not exist', async () => {
    reply([404, {}]);

    await expect(swapStudents(7, 1, 2)).rejects.toThrow('A troca de alunos ainda não está disponível.');
  });
});

describe('teacherGroupApi (#106)', () => {
  const api = teacherGroupApi(10);
  const base = `${API}/teacher/course-units/10/group-contexts`;

  it('uses the routes of the unit', async () => {
    const fetchMock = reply([200, []], [201, {}], [200, {}], [200, {}]);

    await api.list();
    await api.create({ name: 'Projeto', studentIds: [1, 2, 3, 4] });
    await api.generate(20);
    await api.groups(20);

    expect(fetchMock.mock.calls.map(([url, options]) => [url, options.method ?? 'GET'])).toEqual([
      [base, 'GET'],
      [base, 'POST'],
      [`${base}/20/generate`, 'POST'],
      [`${base}/20/groups`, 'GET'],
    ]);
  });

  it('creates, edits and deletes groups with the chosen students', async () => {
    const fetchMock = reply([201, {}], [200, {}], [200, {}]);

    await api.createGroup(20, [1, 2, 3, 4]);
    await api.updateGroup(20, 31, [1, 2, 3, 4, 5]);
    await api.deleteGroup(20, 31);

    const [created, updated, deleted] = fetchMock.mock.calls;
    expect([created[0], created[1].method, JSON.parse(created[1].body)])
      .toEqual([`${base}/20/groups`, 'POST', { studentIds: [1, 2, 3, 4] }]);
    expect([updated[0], updated[1].method, JSON.parse(updated[1].body)])
      .toEqual([`${base}/20/groups/31`, 'PUT', { studentIds: [1, 2, 3, 4, 5] }]);
    expect([deleted[0], deleted[1].method]).toEqual([`${base}/20/groups/31`, 'DELETE']);
  });

  it('shows the message of the backend when a rule fails', async () => {
    reply([409, { message: 'O aluno 4 já está no grupo 1 deste contexto.' }]);

    await expect(api.updateGroup(20, 31, [4])).rejects.toThrow('O aluno 4 já está no grupo 1 deste contexto.');
  });

  it('uses a clear message for a unit of another teacher', async () => {
    reply([403, {}]);

    await expect(api.list()).rejects.toThrow('Só podes gerir grupos das tuas unidades curriculares.');
  });
});
