import { useEffect, useState } from 'react';
import { getStudents } from '../api/studentApi';
import AcademicHistory from './AcademicHistory';
import CompetencyProfile from './CompetencyProfile';
import './StudentRecords.css';

/**
 * Escolhe um estudante e mostra o seu histórico académico (#49)
 * e o perfil de competências com a explicação (#50).
 * Registar uma nota recalcula logo o perfil.
 */
export default function StudentRecords() {
  const [students, setStudents] = useState([]);
  const [studentId, setStudentId] = useState(null);
  const [error, setError] = useState(null);
  const [refreshKey, setRefreshKey] = useState(0);

  useEffect(() => {
    getStudents()
      .then((list) => {
        setStudents(list);
        if (list.length > 0) setStudentId(list[0].id);
      })
      .catch((err) => setError(err.message));
  }, []);

  return (
    <section className="student-records">
      <h2>Percurso e perfil</h2>

      {error && (
        <p className="record-error" role="alert">
          Não foi possível carregar os estudantes ({error}).
        </p>
      )}

      {!error && students.length === 0 && (
        <p className="record-empty">Cria um estudante para registar o histórico e ver o perfil.</p>
      )}

      {students.length > 0 && (
        <>
          <label className="student-picker">
            Estudante
            <select value={studentId ?? ''} onChange={(e) => setStudentId(Number(e.target.value))}>
              {students.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.studentNumber} · {s.name}
                </option>
              ))}
            </select>
          </label>

          {studentId != null && (
            <div className="records-grid">
              <AcademicHistory
                key={`h-${studentId}`}
                studentId={studentId}
                onChange={() => setRefreshKey((k) => k + 1)}
              />
              <CompetencyProfile key={`p-${studentId}`} studentId={studentId} refreshKey={refreshKey} />
            </div>
          )}
        </>
      )}
    </section>
  );
}
