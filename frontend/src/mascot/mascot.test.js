import { describe, expect, it } from 'vitest';
import { BASE_SPRITE, pickMascot } from './mascot';

const c = (code, level) => ({ code, level });

describe('pickMascot', () => {
  it('uses only the base dino when there is no data', () => {
    expect(pickMascot([c('ORG', null)])).toMatchObject({
      layers: [BASE_SPRITE],
      area: null,
      level: 1,
    });
  });

  it('colours the body with the strongest area and stacks every accessory', () => {
    const m = pickMascot([c('ORG', 2), c('TEMPO', 4.4), c('PROG', 3), c('EQUIPA', 1)]);
    expect(m.area.key).toBe('tempo');
    expect(m.level).toBe(4);
    expect(m.tie).toBe(false);
    expect(m.layers[0]).toContain('body-tempo');
    expect(m.layers.slice(1).join(' ')).toMatch(/org-2.*prog-3.*tempo-4/);
    expect(m.layers.join(' ')).not.toContain('equipa');
  });

  it('keeps the base colour while no area reaches level 3', () => {
    const m = pickMascot([c('ORG', 2), c('PROG', 2)]);
    expect(m.layers[0]).toBe(BASE_SPRITE);
    expect(m.layers).toHaveLength(3);
  });

  it('marks a tie and takes the colour from the first area in order', () => {
    const m = pickMascot([c('PROG', 5), c('ORG', 5)]);
    expect(m.area.key).toBe('org');
    expect(m.tie).toBe(true);
    expect(m.layers[0]).toContain('body-org');
  });

  it('keeps the plain base dino when every level rounds to 1', () => {
    expect(pickMascot([c('PROG', 1.2)]).layers).toEqual([BASE_SPRITE]);
  });
});
