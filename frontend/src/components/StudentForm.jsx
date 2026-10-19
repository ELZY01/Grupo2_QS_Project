import { useEffect, useState } from 'react';

const EMPTY_STUDENT = {
  studentNumber: '',
  name: '',
  email: '',
  course: '',
  academicYear: 1,
};

/**
 * Formulário para criar ou editar um estudante.
 *
 * Não comunica com a API: devolve os dados através de onSubmit,
 * e mostra o erro que recebe em `error`.
 */
export default function StudentForm({ student, onSubmit, onCancel, error, saving }) {
  const isEditing = Boolean(student);
  const [values, setValues] = useState(EMPTY_STUDENT);

  // Preenche o formulário quando se escolhe um estudante para editar.
  useEffect(() => {
    setValues(student ? { ...EMPTY_STUDENT, ...student } : EMPTY_STUDENT);
  }, [student]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setValues((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    onSubmit({
      studentNumber: values.studentNumber.trim(),
      name: values.name.trim(),
      email: values.email.trim(),
      course: values.course.trim(),
      academicYear: Number(values.academicYear),
    });
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

      {error && (
        <p className="student-error" role="alert">
          {error}
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
