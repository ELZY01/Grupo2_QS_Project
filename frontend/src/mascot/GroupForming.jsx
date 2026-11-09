import { useState } from 'react';
import Mascot from './Mascot.jsx';
import { AREAS, pickMascot } from './mascot.js';
import './GroupForming.css';

// Esquadra de 4 Pix, um por área, cada um à frente do seu banner.
const SQUAD = Object.keys(AREAS).map((code) => ({
  code,
  key: AREAS[code].key,
  label: AREAS[code].label,
  title: AREAS[code].title,
  layers: pickMascot([{ code, level: 4 }]).layers,
}));

// Confettis do high five: direção (x, y) e cor da área de cada um.
const CONFETTI = [
  [-70, -46, 'org'], [-34, -64, 'tempo'], [0, -72, 'prog'], [34, -64, 'equipa'],
  [70, -46, 'org'], [-56, 10, 'equipa'], [56, 10, 'tempo'], [0, 26, 'prog'],
];

const REDUCED_MOTION_QUERY = '(prefers-reduced-motion: reduce)';

function prefersReducedMotion() {
  return typeof window !== 'undefined'
    && typeof window.matchMedia === 'function'
    && window.matchMedia(REDUCED_MOTION_QUERY).matches;
}

/**
 * Animação mostrada enquanto os grupos são gerados (US17), em estilo "lobby de esquadra":
 * os 4 Pix (organização, tempo, programação e equipa) entram no lobby à frente do seu banner,
 * ficam prontos um a um e fazem um high five ✦ com confettis.
 * De dia há sol; no modo noturno o céu escurece, aparecem estrelas e a lua.
 * Com `prefers-reduced-motion` mostra apenas o texto.
 */
export default function GroupForming({ text = 'A formar grupos…' }) {
  const [reduced] = useState(prefersReducedMotion);

  return (
    <div className="group-forming" role="status" aria-live="polite">
      {!reduced && (
        <div className="lobby" aria-hidden="true" data-testid="group-forming-stage">
          <span className="lobby-sky-body" />
          <span className="lobby-stars" />
          <p className="lobby-header">Grupo formado!</p>
          <div className="lobby-squad">
            {SQUAD.map((pix, index) => (
              <div
                key={pix.code}
                className={`lobby-slot lobby-slot-${pix.key}`}
                style={{ '--i': index }}
              >
                <div className="lobby-banner">
                  <span className="lobby-banner-label" title={pix.label}>{pix.key}</span>
                </div>
                <div className="lobby-pix">
                  <Mascot layers={pix.layers} alt={`Pix ${pix.title}`} size={56} animated={false} />
                </div>
                <span className="lobby-ready">Pronto ✓</span>
              </div>
            ))}
          </div>
          <span className="lobby-spark">✦</span>
          {CONFETTI.map(([x, y, area], i) => (
            <span
              key={i}
              className={`lobby-confetti lobby-confetti-${area}`}
              style={{ '--x': `${x}px`, '--y': `${y}px`, '--r': `${i * 47}deg` }}
            />
          ))}
          <span className="lobby-ground" />
        </div>
      )}
      <div className="lobby-bar">
        <p className="group-forming-text">{text}</p>
      </div>
    </div>
  );
}
