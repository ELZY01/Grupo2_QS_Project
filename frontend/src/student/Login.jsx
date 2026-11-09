import { useState } from 'react';
import { login } from '../api/authApi';

/**
 * Entrar: o aluno usa o número de estudante ou o email; o admin e o docente usam o seu utilizador.
 * A palavra-passe é sempre verificada no backend.
 */
export default function Login({ onBack, onRegister, onLogin }) {
  const [user, setUser] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [searching, setSearching] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setError(null);
    const username = user.trim();

    if (!username) {
      setError('Escreve o teu número de estudante, o email ou o utilizador.');
      return;
    }
    if (!password) {
      setError('Escreve a palavra-passe.');
      return;
    }

    setSearching(true);
    try {
      onLogin(await login(username, password));
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
          Número de estudante, email ou utilizador
          <input value={user} onChange={(e) => setUser(e.target.value)} autoComplete="username" autoFocus />
        </label>

        <label>
          Palavra-passe
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
          />
        </label>

        {error && (
          <p className="sx-error" role="alert">
            {error}
          </p>
        )}

        <div className="sx-actions">
          <button type="submit" className="btn-primary sx-big" disabled={searching}>
            {searching ? 'A entrar…' : 'Entrar'}
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
