import { useState } from 'react';
import { getStudents } from '../api/studentApi';
import { ADMIN_USER, isAdminLogin } from '../auth/session.js';

/**
 * Entrar: o estudante usa o número ou o email; o administrador usa o seu utilizador e palavra-passe.
 */
export default function Login({ onBack, onRegister, onStudent, onAdmin }) {
  const [user, setUser] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [searching, setSearching] = useState(false);

  const isAdmin = user.trim().toLowerCase() === ADMIN_USER.toLowerCase();

  const submit = async (e) => {
    e.preventDefault();
    setError(null);
    const query = user.trim().toLowerCase();

    if (!query) {
      setError('Escreve o teu número de estudante ou o email.');
      return;
    }

    if (isAdmin) {
      if (isAdminLogin(ADMIN_USER, password)) onAdmin();
      else setError('Palavra-passe de administrador errada.');
      return;
    }

    setSearching(true);
    try {
      const students = await getStudents();
      const found = students.find(
        (s) => String(s.studentNumber).toLowerCase() === query || String(s.email).toLowerCase() === query
      );
      if (found) onStudent(found);
      else setError('Não encontrámos ninguém com esses dados. Confirma ou cria o teu perfil.');
    } catch (err) {
      setError(err.message);
    } finally {
      setSearching(false);
    }
  };

  return (
    <main className="sx-center">
      <form className="sx-card sx-form" onSubmit={submit} noValidate>
        <h1>Entrar</h1>
        <p className="sx-muted">O teu Pix está à tua espera.</p>

        <label>
          Número de estudante ou email
          <input value={user} onChange={(e) => setUser(e.target.value)} autoComplete="username" autoFocus />
        </label>

        {isAdmin && (
          <label>
            Palavra-passe
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
            />
          </label>
        )}

        {error && (
          <p className="sx-error" role="alert">
            {error}
          </p>
        )}

        <div className="sx-actions">
          <button type="submit" className="btn-primary sx-big" disabled={searching}>
            {searching ? 'A procurar…' : 'Entrar'}
          </button>
          <button type="button" className="sx-link" onClick={onRegister}>
            Criar perfil
          </button>
          <button type="button" className="sx-link" onClick={onBack}>
            Voltar
          </button>
        </div>
      </form>
    </main>
  );
}
