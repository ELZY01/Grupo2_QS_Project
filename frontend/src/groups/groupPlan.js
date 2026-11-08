// Mesma regra do GroupFormation do backend (#70): k = ⌈n/5⌉ grupos, só de 5 e de 4.
export const MAX_GROUP_SIZE = 5;
export const MIN_GROUP_SIZE = 4;

/**
 * Tamanhos dos grupos para `n` alunos, ou null quando a configuração é impossível
 * (1, 2, 3, 6, 7 e 11 alunos).
 * Ex.: 23 → [5, 5, 5, 4, 4]
 *
 * @param {number} n nº de alunos elegíveis
 * @returns {number[] | null}
 */
export function planGroups(n) {
  if (!Number.isInteger(n) || n < MIN_GROUP_SIZE) return null;
  const k = Math.ceil(n / MAX_GROUP_SIZE);
  if (k * MIN_GROUP_SIZE > n) return null;
  const fives = n - k * MIN_GROUP_SIZE;
  return Array.from({ length: k }, (_, i) => (i < fives ? MAX_GROUP_SIZE : MIN_GROUP_SIZE));
}

/**
 * Texto curto a mostrar no formulário.
 */
export function describePlan(n) {
  if (n === 0) return 'Escolhe os alunos elegíveis.';
  const sizes = planGroups(n);
  if (!sizes) {
    return `Com ${n} aluno${n === 1 ? '' : 's'} não dá para fazer grupos de 4 ou 5.`;
  }
  return `${n} alunos → ${sizes.length} grupo${sizes.length === 1 ? '' : 's'} (${sizes.join(' + ')})`;
}

/**
 * Formata um número com uma casa decimal, à portuguesa (3,4).
 */
export function formatDecimal(value) {
  return Number(value).toLocaleString('pt-PT', { minimumFractionDigits: 1, maximumFractionDigits: 1 });
}

/**
 * Diferença para os outros grupos, com sinal (+0,2 / −0,1 / 0,0).
 */
export function formatGap(value) {
  const rounded = Math.round(Number(value) * 10) / 10;
  if (rounded === 0) return formatDecimal(0);
  return `${rounded > 0 ? '+' : '−'}${formatDecimal(Math.abs(rounded))}`;
}
