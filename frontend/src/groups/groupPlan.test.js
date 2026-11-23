import { describe, expect, it } from 'vitest';
import {
  describePlan, formatDecimal, formatGap, groupOf, groupSizeError, planGroups, swapError,
} from './groupPlan.js';

describe('planGroups', () => {
  it('rejects the impossible configurations', () => {
    [0, 1, 2, 3, 6, 7, 11].forEach((n) => expect(planGroups(n)).toBeNull());
  });

  it('only makes groups of 5 and 4', () => {
    expect(planGroups(4)).toEqual([4]);
    expect(planGroups(5)).toEqual([5]);
    expect(planGroups(8)).toEqual([4, 4]);
    expect(planGroups(10)).toEqual([5, 5]);
    expect(planGroups(23)).toEqual([5, 5, 5, 4, 4]);
  });

  it('handles the 11/12 boundary', () => {
    expect(planGroups(11)).toBeNull();
    expect(planGroups(12)).toEqual([4, 4, 4]);
  });
});

describe('describePlan', () => {
  it('explains the split or why it is impossible', () => {
    expect(describePlan(0)).toBe('Escolhe os alunos elegíveis.');
    expect(describePlan(23)).toBe('23 alunos → 5 grupos (5 + 5 + 5 + 4 + 4)');
    expect(describePlan(7)).toBe('Com 7 alunos não dá para fazer grupos de 4 ou 5.');
    expect(describePlan(1)).toBe('Com 1 aluno não dá para fazer grupos de 4 ou 5.');
  });
});

describe('formatting', () => {
  it('uses a comma and one decimal place', () => {
    expect(formatDecimal(3.46)).toBe('3,5');
    expect(formatDecimal(4)).toBe('4,0');
  });

  it('shows the sign of the gap', () => {
    expect(formatGap(0.24)).toBe('+0,2');
    expect(formatGap(-0.1)).toBe('−0,1');
    expect(formatGap(0.01)).toBe('0,0');
  });

  it('rounds two opposite gaps to the same size (#106)', () => {
    // Valores reais da API depois de uma edição: −0,05 e +0,05 com erro de vírgula flutuante.
    expect(formatGap(-0.04999999999999982)).toBe('−0,1');
    expect(formatGap(0.05000000000000071)).toBe('+0,1');
  });
});

describe('swapError (#75)', () => {
  const groups = [
    { number: 1, members: [{ studentId: 1 }, { studentId: 2 }] },
    { number: 2, members: [{ studentId: 3 }, { studentId: 4 }] },
  ];

  it('finds the group of a student', () => {
    expect(groupOf(groups, 3)).toBe(2);
    expect(groupOf(groups, 99)).toBeNull();
  });

  it('accepts two students from different groups', () => {
    expect(swapError(groups, 1, 3)).toBeNull();
  });

  it('asks for both students', () => {
    expect(swapError(groups, 1, null)).toBe('Escolhe os dois alunos a trocar.');
  });

  it('rejects two students from the same group', () => {
    expect(swapError(groups, 1, 2)).toBe('Escolhe alunos de grupos diferentes.');
  });

  it('rejects a student that is not in any group', () => {
    expect(swapError(groups, 1, 99)).toBe('Aluno não encontrado nos grupos.');
  });
});

describe('groupSizeError (#106)', () => {
  it('accepts groups of 4 and 5 students', () => {
    expect(groupSizeError(4)).toBeNull();
    expect(groupSizeError(5)).toBeNull();
  });

  it('rejects smaller and bigger groups with the chosen size', () => {
    expect(groupSizeError(3)).toBe('Um grupo tem de ter entre 4 e 5 alunos (escolheste 3).');
    expect(groupSizeError(6)).toBe('Um grupo tem de ter entre 4 e 5 alunos (escolheste 6).');
  });
});
