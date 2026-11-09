import { useCallback, useEffect, useState } from 'react';
import { getProfile } from '../api/profileApi';
import AcademicHistory from '../components/AcademicHistory';
import CompetencyProfile from '../components/CompetencyProfile';
import '../components/StudentRecords.css';
import Mascot from '../mascot/Mascot.jsx';
import { AREAS, pickMascot } from '../mascot/mascot.js';

// O que cada área acrescenta ao Pix em cada nível (igual à folha de design).
const UNLOCKS = {
  ORG: { 2: 'manchas azuis', 3: 'corpo azul', 4: 'prancheta', 5: 'caneca de café' },
  TEMPO: { 2: 'crista laranja', 3: 'corpo amarelo', 4: 'relógio de bolso', 5: 'brilhos dourados' },
  PROG: { 2: 'faísca de código', 3: 'corpo verde-água e portátil', 4: 'monóculo', 5: 'cartola' },
  EQUIPA: { 2: 'corações na barriga', 3: 'corpo rosa', 4: 'cachecol', 5: 'balão de coração' },
};

const TABS = [
  { id: 'pix', label: 'O meu Pix' },
  { id: 'grades', label: 'As minhas cadeiras' },
  { id: 'profile', label: 'Como é calculado' },
];

/**
 * Página do estudante depois de o Pix nascer.
 */
export default function StudentHome({ studentId, onLogout }) {
  const [tab, setTab] = useState('pix');
  const [profile, setProfile] = useState(null);
  const [error, setError] = useState(null);

  const load = useCallback(() => {
    getProfile(studentId)
      .then(setProfile)
      .catch((err) => setError(err.message));
  }, [studentId]);

  useEffect(() => {
    load();
  }, [load]);

  const mascot = profile ? pickMascot(profile.competencies) : null;
  const firstName = profile?.studentName?.split(' ')[0] ?? '';

  return (
    <div className="sx-home">
      <header className="sx-topbar">
        <p className="sx-brand">StudyMatch</p>
        <nav className="sx-tabs" aria-label="A minha área">
          {TABS.map((t) => (
            <button
              key={t.id}
              type="button"
              className={tab === t.id ? 'active' : ''}
              aria-current={tab === t.id ? 'page' : undefined}
              onClick={() => setTab(t.id)}
            >
              {t.label}
            </button>
          ))}
        </nav>
        <button type="button" className="sx-link" onClick={onLogout}>
          Sair
        </button>
      </header>

      <main className="sx-page">
        {error && (
          <p className="sx-error" role="alert">
            Não foi possível carregar o teu perfil ({error}).
          </p>
        )}

        {tab === 'pix' && profile && mascot && (
          <>
            <section className="sx-card sx-pix">
              <div className="sx-pix-art">
                <Mascot layers={mascot.layers} alt={`O teu Pix: ${mascot.area?.title ?? 'bebé'}`} size={190} />
              </div>
              <div className="sx-pix-text">
                <p className="sx-muted">Olá, {firstName}!</p>
                <h1>{mascot.area ? `O teu Pix é ${mascot.area.title}` : 'O teu Pix ainda é bebé'}</h1>
                <p className="sx-lead">
                  {mascot.area
                    ? `A cor vem da tua área mais forte (${mascot.area.label}). Os acessórios mostram as outras. Quando o administrador lançar as tuas notas, o Pix evolui.`
                    : 'Quando o administrador lançar as tuas notas, o Pix ganha cor e acessórios.'}
                </p>
                <button type="button" className="btn-primary" onClick={() => setTab('grades')}>
                  Ver as minhas cadeiras
                </button>
              </div>
            </section>

            <section className="sx-card sx-areas" aria-labelledby="areas-title">
              <div className="sx-areas-head">
                <h2 id="areas-title">Competências</h2>
                <p className="sx-muted sx-small">Cada nível acrescenta um acessório ao Pix.</p>
              </div>
              <ul className="sx-area-list">
                {Object.entries(AREAS).map(([code, area]) => {
                  const level = mascot.levels[code] ?? 1;
                  const next = level < 5 ? UNLOCKS[code][level + 1] : null;
                  return (
                    <li key={code} className={`sx-area sx-area-${area.key}`}>
                      <span className="sx-area-name">
                        <span className="sx-area-swatch" aria-hidden="true" />
                        {area.label}
                      </span>
                      <span className="sx-dots" role="img" aria-label={`Nível ${level} de 5`}>
                        {[1, 2, 3, 4, 5].map((n) => (
                          <span key={n} className={n <= level ? 'on' : ''} />
                        ))}
                      </span>
                      <span className="sx-level mono">{level}/5</span>
                      <span className="sx-area-next">
                        {next ? (
                          <>
                            Nível {level + 1}: <strong>{next}</strong>
                          </>
                        ) : (
                          'Todos os acessórios desbloqueados'
                        )}
                      </span>
                    </li>
                  );
                })}
              </ul>
            </section>
          </>
        )}

        {tab === 'grades' && (
          <AcademicHistory studentId={studentId} readOnly />
        )}

        {tab === 'profile' && <CompetencyProfile studentId={studentId} />}
      </main>
    </div>
  );
}
