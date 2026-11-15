import { useState } from 'react';
import { register } from '../api/authApi';
import Egg from '../mascot/Egg.jsx';

const EMPTY = { name: '', studentNumber: '', email: '', course: '', academicYear: 1, password: '', confirm: '' };
const MIN_PASSWORD = 8;

/**
 * O estudante cria o seu perfil e a palavra-passe. A seguir vai direto para o quiz.
 * onCreated recebe a sessão devolvida pelo backend (já com o token).
 */
export default function Register({ onBack, onCreated }) {
  const [values, setValues] = useState(EMPTY);
  const [error, setError] = useState(null);
  const [saving, setSaving] = useState(false);

  const change = (e) => setValues((v) => ({ ...v, [e.target.name]: e.target.value }));

  const submit = async (e) => {
    e.preventDefault();
    setError(null);
    if (values.password.length < MIN_PASSWORD) {
      setError(`A palavra-passe tem de ter pelo menos ${MIN_PASSWORD} caracteres.`);
      return;
    }
    if (values.password !== values.confirm) {
      setError('As palavras-passe não são iguais.');
      return;
    }
    setSaving(true);
    try {
      const session = await register({
        name: values.name.trim(),
        studentNumber: values.studentNumber.trim(),
        email: values.email.trim(),
        course: values.course.trim(),
        academicYear: Number(values.academicYear),
        password: values.password,
      });
      onCreated(session);
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <main className="sx-center">
      <form className="sx-card sx-form" onSubmit={submit} noValidate>
        <div className="sx-card-head">
          <Egg stage={0} size={64} label="O Pix dentro do ovo" />
          <div>
            <h1>Vamos conhecer-te</h1>
            <p className="sx-muted">Só precisamos do básico. Depois vem o quiz.</p>
          </div>
        </div>

        <label>
          Como te chamas?
          <input name="name" value={values.name} onChange={change} autoComplete="name" />
        </label>

        <div className="sx-row">
          <label>
            Número de estudante
            <input name="studentNumber" value={values.studentNumber} onChange={change} />
          </label>
          <label>
            Ano
            <select name="academicYear" value={values.academicYear} onChange={change}>
              {[1, 2, 3, 4, 5].map((y) => (
                <option key={y} value={y}>
                  {y}.º ano
                </option>
              ))}
            </select>
          </label>
        </div>

        <label>
          Email da universidade
          <input name="email" type="email" value={values.email} onChange={change} autoComplete="email" />
        </label>

        <label>
          Curso
          <input name="course" value={values.course} onChange={change} placeholder="Ex.: Engenharia Informática" />
        </label>

        <div className="sx-row">
          <label>
            Palavra-passe
            <input name="password" type="password" value={values.password} onChange={change} autoComplete="new-password" />
          </label>
          <label>
            Repete a palavra-passe
            <input name="confirm" type="password" value={values.confirm} onChange={change} autoComplete="new-password" />
          </label>
        </div>

        {error && (
          <p className="sx-error" role="alert">
            {error}
          </p>
        )}

        <div className="sx-actions">
          <button type="submit" className="btn-primary sx-big" disabled={saving}>
            {saving ? 'A criar…' : 'Continuar para o quiz'}
          </button>
          <button type="button" className="sx-link" onClick={onBack}>
            Voltar
          </button>
        </div>
      </form>
    </main>
  );
}
