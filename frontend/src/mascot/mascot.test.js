import { describe, expect, it } from 'vitest';
import { BASE_SPRITE, pickMascot } from './mascot';

const c = (code, level) => ({ code, level });

describe('pickMascot', () => {
  it('uses the base dino when there is no data', () => {
    expect(pickMascot([c('ORG', null)])).toMatchObject({ src: BASE_SPRITE, area: null, level: 1 });
  });

  it('shows the strongest area at its rounded level', () => {
    const m = pickMascot([c('ORG', 2), c('TEMPO', 4.4), c('PROG', 3), c('EQUIPA', 1)]);
    expect(m.area.key).toBe('tempo');
    expect(m.level).toBe(4);
    expect(m.src).toContain('tempo-4');
    expect(m.tie).toBe(false);
  });

  it('marks a tie and uses the first area in order', () => {
    const m = pickMascot([c('PROG', 5), c('ORG', 5)]);
    expect(m.area.key).toBe('org');
    expect(m.tie).toBe(true);
  });

  it('keeps the base dino when the best level rounds to 1', () => {
    expect(pickMascot([c('PROG', 1.2)]).src).toBe(BASE_SPRITE);
  });
});
