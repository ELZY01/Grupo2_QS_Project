import { useEffect, useState } from 'react';
import { getProfile } from '../api/profileApi';
import Egg from './Egg.jsx';
import Mascot from './Mascot.jsx';
import { BASE_SPRITE, SHELL_SPRITE, pickMascot } from './mascot.js';
import { playRoar } from './roar.js';

const CRACK_MS = 1400;

/**
 * Momento em que o Pix sai do ovo, depois de o quiz ser submetido.
 * O ovo abana, abre, o Pix faz "Rawwwr!" e aparece já com o perfil do estudante.
 */
export default function Hatch({ studentId, onViewProfile, onRedo, viewLabel = 'Ver perfil' }) {
  const [phase, setPhase] = useState('cracking');
  const [profile, setProfile] = useState(null);

  useEffect(() => {
    let alive = true;
    getProfile(studentId)
      .then((p) => alive && setProfile(p))
      .catch(() => {});
    const timer = setTimeout(() => {
      if (!alive) return;
      setPhase('hatched');
      playRoar();
    }, CRACK_MS);
    return () => {
      alive = false;
      clearTimeout(timer);
    };
  }, [studentId]);

  const mascot = profile ? pickMascot(profile.competencies) : null;
  const layers = mascot ? mascot.layers : [BASE_SPRITE];
  const title = mascot?.area ? `Pix ${mascot.area.title} · nível ${mascot.level}` : 'Pix';

  return (
    <section className={`card hatch ${phase}`} aria-live="polite">
      <div className="hatch-stage">
        {phase === 'cracking' ? (
          <Egg stage={4} size={150} label="O ovo está a abrir" />
        ) : (
          <>
            <p className="roar" aria-hidden="true">RAWWWR!</p>
            <Mascot layers={[...layers, SHELL_SPRITE]} alt={`O Pix nasceu: ${title}`} size={150} />
          </>
        )}
      </div>

      {phase === 'cracking' ? (
        <h2>O ovo está a abrir…</h2>
      ) : (
        <>
          <h2>O Pix nasceu!</h2>
          <p>
            {profile
              ? `${profile.studentName}, o teu Pix é ${title}. Ele vai evoluir com as tuas notas.`
              : 'As tuas competências foram registadas.'}
          </p>
          <div className="actions">
            <button type="button" className="btn-primary" onClick={onViewProfile}>
              {viewLabel}
            </button>
            {onRedo && (
              <button type="button" onClick={onRedo}>
                Refazer quiz
              </button>
            )}
          </div>
        </>
      )}
    </section>
  );
}
