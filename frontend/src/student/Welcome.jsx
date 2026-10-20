import Egg from '../mascot/Egg.jsx';

/**
 * Primeiro ecrã do estudante: o Pix ainda é um ovo e apresenta a app.
 */
export default function Welcome({ onStart, onLogin }) {
  return (
    <main className="sx-center sx-welcome">
      <div className="sx-hero">
        <div className="sx-hero-pix">
          <p className="sx-bubble">Olá! Ainda estou dentro do ovo… ajudas-me a nascer?</p>
          <Egg stage={1} size={170} label="O Pix dentro do ovo" />
        </div>

        <div className="sx-hero-text">
          <p className="sx-brand">StudyMatch</p>
          <h1>Encontra o teu grupo ideal e cria o teu Pix</h1>
          <p className="sx-lead">
            Responde a um quiz rápido sobre como trabalhas. Cada resposta racha a casca do ovo e, no
            fim, nasce o Pix: o teu dinossauro, com as cores e acessórios das tuas competências.
          </p>

          <ol className="sx-steps">
            <li><span>1</span> Cria o teu perfil</li>
            <li><span>2</span> Responde ao quiz</li>
            <li><span>3</span> Conhece o teu Pix</li>
          </ol>

          <div className="sx-actions">
            <button type="button" className="btn-primary sx-big" onClick={onStart}>
              Começar
            </button>
            <button type="button" className="sx-link" onClick={onLogin}>
              Já tenho conta
            </button>
          </div>
        </div>
      </div>
    </main>
  );
}
