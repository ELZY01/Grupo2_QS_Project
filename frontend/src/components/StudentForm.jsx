import { useEffect, useState } from 'react';

const EMPTY_STUDENT = {
  studentNumber: '',
  name: '',
  email: '',
  course: '',
  academicYear: 1,
  password: '',
};

const MIN_PASSWORD = 8;

/**
 * Formulário para criar ou editar um estudante.
 *
 * Não comunica com a API: devolve os dados e a palavra-passe através de onSubmit(data, password),
 * e mostra o erro que recebe em `error`.
 * A palavra-passe é obrigatória ao criar; ao editar só muda se for preenchida.
 */
export default function StudentForm({ student, onSubmit, onCancel, error, saving }) {
  const isEditing = Boolean(student);
  const [values, setValues] = useState(EMPTY_STUDENT);
  const [passwordError, setPasswordError] = useState(null);

  // Preenche o formulário quando se escolhe um estudante para editar.
  useEffect(() => {
    setValues(student ? { ...EMPTY_STUDENT, ...student, password: '' } : EMPTY_STUDENT);
    setPasswordError(null);
  }, [student]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setValues((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    const password = values.password;
    if ((!isEditing || password) && password.length < MIN_PASSWORD) {
      setPasswordError(`A palavra-passe tem de ter pelo menos ${MIN_PASSWORD} caracteres.`);
      return;
    }
    setPasswordError(null);
    onSubmit({
      studentNumber: values.studentNumber.trim(),
      name: values.name.trim(),
      email: values.email.trim(),
      course: values.course.trim(),
      academicYear: Number(values.academicYear),
    }, password || null);
  };

  return (
    <form className="student-form" onSubmit={handleSubmit} noValidate>
      <h3>{isEditing ? 'Editar estudante' : 'Novo estudante'}</h3>

      <label>
        Número de estudante
        <input name="studentNumber" value={values.studentNumber} onChange={handleChange} />
      </label>

      <label>
        Nome
        <input name="name" value={values.name} onChange={handleChange} />
      </label>

      <label>
        Email
        <input name="email" type="email" value={values.email} onChange={handleChange} />
      </label>

      <label>
        Curso
        <input name="course" value={values.course} onChange={handleChange} />
      </label>

      <label>
        Ano curricular
        <input
          name="academicYear"
          type="number"
          min="1"
          value={values.academicYear}
          onChange={handleChange}
        />
      </label>

      <label>
        {isEditing ? 'Nova palavra-passe (opcional)' : 'Palavra-passe inicial'}
        <input
          name="password"
          type="password"
          value={values.password}
          onChange={handleChange}
          autoComplete="new-password"
          placeholder={isEditing ? 'Deixa vazio para manter' : 'Mínimo 8 caracteres'}
        />
      </label>

      {(passwordError || error) && (
        <p className="student-error" role="alert">
          {passwordError || error}
        </p>
      )}

      <div className="student-form-actions">
        <button type="submit" disabled={saving}>
          {isEditing ? 'Guardar alterações' : 'Criar estudante'}
        </button>
        {isEditing && (
          <button type="button" onClick={onCancel} disabled={saving}>
            Cancelar
          </button>
        )}
      </div>
    </form>
  );
}
