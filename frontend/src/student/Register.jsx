import { useState } from 'react';
import { createStudent } from '../api/studentApi';
import Egg from '../mascot/Egg.jsx';

const EMPTY = { name: '', studentNumber: '', email: '', course: '', academicYear: 1 };

/**
 * O estudante cria o seu perfil. A seguir vai direto para o quiz.
 */
export default function Register({ onBack, onCreated }) {
  const [values, setValues] = useState(EMPTY);
  const [error, setError] = useState(null);
  const [saving, setSaving] = useState(false);

  const change = (e) => setValues((v) => ({ ...v, [e.target.name]: e.target.value }));

  const submit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    try {
      const created = await createStudent({
        name: values.name.trim(),
        studentNumber: values.studentNumber.trim(),
        email: values.email.trim(),
        course: values.course.trim(),
        academicYear: Number(values.academicYear),
      });
      onCreated(created);
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
