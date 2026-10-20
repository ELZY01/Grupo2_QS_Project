import Mascot from './Mascot';
import { BASE_SPRITE } from './mascot';

// Dicas da mascote para quem está a começar (tutorial).
const TIPS = {
  students: 'Olá! Sou o Pix. Começa por criar o teu registo de estudante.',
  records: 'Regista as tuas UCs e notas. Eu evoluo quando as tuas competências sobem!',
  quiz: 'Ainda não tens notas? Responde ao quiz e eu nasço com o teu perfil.',
};

export default function MascotGuide({ tab }) {
  return (
    <aside className="mascot-guide" aria-label="Dica da mascote">
      <p className="mascot-bubble">{TIPS[tab]}</p>
      <Mascot src={BASE_SPRITE} alt="Pix, a mascote do StudyMatch" size={84} />
    </aside>
  );
}
