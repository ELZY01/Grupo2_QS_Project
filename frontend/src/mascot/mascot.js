// Camadas da mascote (pixel art 57x66), geradas a partir da folha de design (#35).
// - base.png: dinossauro de nível 1
// - body-<área>.png: corpo pintado com a cor da área
// - <área>-<nível>.png: acessórios dessa área nesse nível (fundo transparente)
const sprites = import.meta.glob('../assets/mascot/*.png', { eager: true, import: 'default' });
const sprite = (name) => sprites[`../assets/mascot/${name}.png`];

export const MASCOT_WIDTH = 57;
export const MASCOT_HEIGHT = 66;

// Áreas base, pela ordem usada em caso de empate.
export const AREAS = {
  ORG: { key: 'org', label: 'Organização', title: 'organizado' },
  TEMPO: { key: 'tempo', label: 'Gestão de tempo', title: 'pontual' },
  PROG: { key: 'prog', label: 'Programação', title: 'programador' },
  EQUIPA: { key: 'equipa', label: 'Trabalho em grupo', title: 'team player' },
};

// Ordem em que os acessórios são desenhados (o último fica por cima).
const LAYER_ORDER = ['EQUIPA', 'TEMPO', 'PROG', 'ORG'];

// A partir deste nível a área pinta o corpo todo.
export const COLOR_LEVEL = 3;

export const BASE_SPRITE = sprite('base');

const toLevel = (value) => Math.min(5, Math.max(1, Math.round(value)));

/**
 * Monta a mascote a partir das competências do perfil.
 * - Cada área acrescenta os seus acessórios conforme o nível (2 a 5).
 * - A cor do corpo vem da área mais forte, a partir do nível 3.
 * - Em caso de empate, a cor vem da primeira área pela ordem de AREAS e `tie` fica true.
 *
 * @param {Array<{code: string, level: number|null}>} competencies
 * @returns {{ layers: string[], src: string, area: object|null, level: number,
 *             tie: boolean, levels: Record<string, number> }}
 */
export function pickMascot(competencies = []) {
  const levels = {};
  for (const c of competencies) {
    if (AREAS[c.code] && c.level != null) levels[c.code] = toLevel(c.level);
  }

  const codes = Object.keys(AREAS).filter((code) => levels[code] != null);
  if (codes.length === 0) {
    return { layers: [BASE_SPRITE], src: BASE_SPRITE, area: null, level: 1, tie: false, levels };
  }

  const max = Math.max(...codes.map((code) => levels[code]));
  const top = codes.filter((code) => levels[code] === max);
  const dominant = top[0];
  const colored = max >= COLOR_LEVEL;

  const body = colored ? sprite(`body-${AREAS[dominant].key}`) : BASE_SPRITE;
  // Acessórios por ordem fixa: primeiro o que pinta a barriga (equipa), depois os objetos
  // (relógio, portátil, prancheta), para a prancheta nunca ficar pintada por baixo.
  const order = LAYER_ORDER.filter((code) => codes.includes(code));
  const accessories = order
    .filter((code) => levels[code] >= 2)
    .map((code) => sprite(`${AREAS[code].key}-${levels[code]}`));

  return {
    layers: [body, ...accessories],
    src: body,
    area: max === 1 ? null : AREAS[dominant],
    level: max,
    tie: max > 1 && top.length > 1,
    levels,
  };
}

// Ovo do quiz inicial: racha à medida que o estudante responde (#35).
export const EGG_STAGES = [0, 1, 2, 3, 4].map((n) => sprite(`egg-${n}`));
export const SHELL_SPRITE = sprite('shell');

/**
 * Fase do ovo (0 = inteiro, 4 = quase a abrir) a partir das respostas dadas.
 * @param {number} answered perguntas respondidas
 * @param {number} total total de perguntas
 */
export function eggStage(answered, total) {
  if (!total || answered <= 0) return 0;
  return Math.min(4, Math.floor((answered / total) * 5));
}

// Pix do administrador: versão ultra rara (roxo real, coroa e brilhos dourados). Não passa pelo quiz.
export const ADMIN_SPRITE = sprite('admin');
