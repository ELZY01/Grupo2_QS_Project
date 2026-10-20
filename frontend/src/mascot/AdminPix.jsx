import Mascot from './Mascot.jsx';
import { ADMIN_SPRITE } from './mascot.js';

const TIPS = {
  students: 'Bem-vindo, admin! Aqui geres os estudantes. Eles próprios fazem o quiz quando entram.',
  records: 'Lança as cadeiras e as notas de cada estudante. O Pix deles evolui com isto!',
};

/**
 * Pix do administrador: a versão ultra rara, que não nasce de um ovo.
 */
export default function AdminPix({ tab }) {
  return (
    <aside className="mascot-guide admin-pix" aria-label="Dica da mascote">
      <p className="mascot-bubble">{TIPS[tab]}</p>
      <div className="admin-pix-art">
        <Mascot src={ADMIN_SPRITE} alt="Pix Admin, versão ultra rara" size={96} />
        <p className="admin-badge">★ Ultra raro · Pix Admin</p>
      </div>
    </aside>
  );
}
