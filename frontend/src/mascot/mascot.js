// Sprites da mascote (pixel art 53x60), gerados a partir da folha de design (#35).
const sprites = import.meta.glob('../assets/mascot/*.png', { eager: true, import: 'default' });
const sprite = (name) => sprites[`../assets/mascot/${name}.png`];

// Áreas base, pela ordem usada em caso de empate.
export const AREAS = {
  ORG: { key: 'org', label: 'Organização', title: 'organizado' },
  TEMPO: { key: 'tempo', label: 'Gestão de tempo', title: 'pontual' },
  PROG: { key: 'prog', label: 'Programação', title: 'programador' },
  EQUIPA: { key: 'equipa', label: 'Trabalho em grupo', title: 'team player' },
};

export const BASE_SPRITE = sprite('base');

/**
 * Escolhe a mascote a partir das competências do perfil.
 * Versão 1 (folha de design atual): mostra a área mais forte, no nível dessa área.
 * Em caso de empate usa a primeira área pela ordem de AREAS e marca `tie`.
 *
 * @param {Array<{code: string, level: number|null}>} competencies
 * @returns {{ src: string, area: object|null, level: number, tie: boolean }}
 */
export function pickMascot(competencies = []) {
  const known = competencies.filter((c) => AREAS[c.code] && c.level != null);
  if (known.length === 0) {
    return { src: BASE_SPRITE, area: null, level: 1, tie: false };
  }

  const max = Math.max(...known.map((c) => c.level));
  const top = Object.keys(AREAS).filter((code) =>
    known.some((c) => c.code === code && c.level === max)
  );
  const level = Math.min(5, Math.max(1, Math.round(max)));
  const area = AREAS[top[0]];

  return {
    src: level === 1 ? BASE_SPRITE : sprite(`${area.key}-${level}`),
    area: level === 1 ? null : area,
    level,
    tie: top.length > 1,
  };
}
