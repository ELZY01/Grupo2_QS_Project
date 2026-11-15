import { describe, expect, it } from 'vitest';
import { describePlan, formatDecimal, formatGap, planGroups } from './groupPlan.js';

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
});
