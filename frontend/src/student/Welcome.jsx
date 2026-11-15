import Egg from '../mascot/Egg.jsx';

const STEPS = [
  { title: 'Cria o teu perfil', text: 'Nome, número de estudante e curso. Um minuto.' },
  { title: 'Responde ao quiz', text: 'Perguntas rápidas sobre como estudas e trabalhas em grupo.' },
  { title: 'Conhece o teu Pix', text: 'Nasce com as cores das tuas competências e evolui com as notas.' },
];

/**
 * Primeiro ecrã do estudante: o Pix ainda é um ovo e apresenta a app.
 */
export default function Welcome({ onStart, onLogin }) {
  return (
    <div className="sx-welcome">
      <header className="sx-authbar">
        <p className="sx-brand">StudyMatch</p>
        <button type="button" className="sx-link" onClick={onLogin}>
          Entrar
        </button>
      </header>

      <main className="sx-hero">
        <section className="sx-hero-text">
          <h1>Grupos de estudo formados pelo que sabes fazer, não por quem está ao teu lado.</h1>
          <p className="sx-lead">
            O StudyMatch junta o que dizes sobre ti com as tuas notas para propor grupos equilibrados.
            Pelo caminho, choca um ovo: o Pix, o teu dinossauro, que muda com as tuas competências.
          </p>
          <div className="sx-actions">
            <button type="button" className="btn-primary sx-big" onClick={onStart}>
              Criar perfil
            </button>
            <button type="button" className="sx-big" onClick={onLogin}>
              Já tenho conta
            </button>
          </div>
        </section>

        <aside className="sx-hero-pix" aria-label="O Pix">
          <p className="sx-bubble">Ainda estou dentro do ovo… ajudas-me a nascer?</p>
          <Egg stage={1} size={150} label="O Pix dentro do ovo" />
          <p className="sx-hero-caption mono">PIX · ainda no ovo</p>
        </aside>
      </main>

      <ol className="sx-steps" aria-label="Como funciona">
        {STEPS.map((step, i) => (
          <li key={step.title}>
            <span className="sx-step-n mono">{String(i + 1).padStart(2, '0')}</span>
            <div>
              <p className="sx-step-title">{step.title}</p>
              <p className="sx-step-text">{step.text}</p>
            </div>
          </li>
        ))}
      </ol>
    </div>
  );
}
