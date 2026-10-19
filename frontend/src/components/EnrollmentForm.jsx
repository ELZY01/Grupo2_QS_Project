import { useState } from 'react';

const CURRENT_SCHOOL_YEAR = (() => {
  const now = new Date();
  // O ano letivo começa em setembro: em outubro de 2026 é 2026/2027.
  return now.getMonth() >= 8 ? now.getFullYear() : now.getFullYear() - 1;
})();

const EMPTY = {
  courseUnitId: '',
  schoolYear: CURRENT_SCHOOL_YEAR,
  semester: 1,
  status: 'APROVADO',
  grade: '',
};

/**
 * Formulário para registar uma inscrição (uma tentativa numa UC).
 * Não comunica com a API: devolve os dados por onSubmit e mostra o erro que recebe.
 */
export default function EnrollmentForm({ courseUnits, onSubmit, error, saving }) {
  const [values, setValues] = useState(EMPTY);
  const inProgress = values.status === 'EM_CURSO';

  const handleChange = (event) => {
    const { name, value } = event.target;
    setValues((current) => ({
      ...current,
      [name]: value,
      // Uma inscrição em curso não tem nota.
      ...(name === 'status' && value === 'EM_CURSO' ? { grade: '' } : {}),
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    const saved = await onSubmit({
      courseUnitId: values.courseUnitId === '' ? null : Number(values.courseUnitId),
      schoolYear: Number(values.schoolYear),
      semester: Number(values.semester),
      status: values.status,
      grade: inProgress || values.grade === '' ? null : Number(values.grade),
    });
    if (saved) {
      setValues((current) => ({ ...EMPTY, schoolYear: current.schoolYear, semester: current.semester }));
    }
  };

  return (
    <form className="record-form" onSubmit={handleSubmit} noValidate>
      <h4>Nova inscrição</h4>

      <label>
        Unidade curricular
        <select name="courseUnitId" value={values.courseUnitId} onChange={handleChange}>
          <option value="">Escolher UC…</option>
          {courseUnits.map((uc) => (
            <option key={uc.id} value={uc.id}>
              {uc.code} · {uc.name}
            </option>
          ))}
        </select>
      </label>

      <label>
        Ano letivo (início)
        <input
          name="schoolYear"
          type="number"
          min="2000"
          value={values.schoolYear}
          onChange={handleChange}
        />
      </label>

      <label>
        Semestre
        <select name="semester" value={values.semester} onChange={handleChange}>
          <option value="1">1.º semestre</option>
          <option value="2">2.º semestre</option>
        </select>
      </label>

      <label>
        Estado
        <select name="status" value={values.status} onChange={handleChange}>
          <option value="APROVADO">Aprovado</option>
          <option value="REPROVADO">Reprovado</option>
          <option value="EM_CURSO">Em curso</option>
        </select>
      </label>

      <label>
        Nota (0–20)
        <input
          name="grade"
          type="number"
          min="0"
          max="20"
          value={values.grade}
          onChange={handleChange}
          disabled={inProgress}
          placeholder={inProgress ? 'Sem nota' : ''}
        />
      </label>

      {error && (
        <p className="record-error" role="alert">
          {error}
        </p>
      )}

      <div className="record-form-actions">
        <button type="submit" disabled={saving}>
          {saving ? 'A guardar…' : 'Registar inscrição'}
        </button>
      </div>
    </form>
  );
}
