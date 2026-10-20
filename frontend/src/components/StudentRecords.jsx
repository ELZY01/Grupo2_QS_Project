import { useEffect, useState } from 'react';
import { getProfile } from '../api/profileApi';
import { getStudents } from '../api/studentApi';
import Egg from '../mascot/Egg.jsx';
import AcademicHistory from './AcademicHistory';
import CompetencyProfile from './CompetencyProfile';
import './StudentRecords.css';

/**
 * Escolhe um estudante e mostra o seu histórico académico (#49)
 * e o perfil de competências com a explicação (#50).
 * Registar uma nota recalcula logo o perfil.
 * O administrador lança aqui as cadeiras e notas. Se o estudante ainda não fez
 * o quiz inicial, aparece um aviso: o Pix dele ainda está no ovo.
 */
export default function StudentRecords({ initialStudentId = null }) {
  const [students, setStudents] = useState([]);
  const [studentId, setStudentId] = useState(initialStudentId);
  // null = a verificar, true/false = já respondeu ou não ao quiz
  const [quizDone, setQuizDone] = useState(null);
  const [error, setError] = useState(null);
  const [refreshKey, setRefreshKey] = useState(0);

  useEffect(() => {
    getStudents()
      .then((list) => {
        setStudents(list);
        setStudentId((current) => current ?? (list.length > 0 ? list[0].id : null));
      })
      .catch((err) => setError(err.message));
  }, []);

  useEffect(() => {
    if (studentId == null) return;
    let alive = true;
    setQuizDone(null);
    getProfile(studentId)
      .then((p) => alive && setQuizDone(Boolean(p.quizCompleted)))
      // Se o perfil falhar, não bloqueia: o erro aparece no próprio perfil.
      .catch(() => alive && setQuizDone(true));
    return () => {
      alive = false;
    };
  }, [studentId]);

  const student = students.find((s) => s.id === studentId);

  return (
    <section className="student-records">
      <h2>Cadeiras e notas</h2>

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

          {studentId != null && quizDone === false && (
            <section className="card egg-gate">
              <Egg stage={0} size={72} label="O Pix ainda está no ovo" />
              <div>
                <h3>O Pix {student ? `de ${student.name} ` : ''}ainda está no ovo</h3>
                <p>
                  Este estudante ainda não respondeu ao quiz inicial. Podes lançar as cadeiras na
                  mesma; quando ele entrar na app, o quiz é obrigatório antes de tudo o resto.
                </p>
              </div>
            </section>
          )}

          {studentId != null && quizDone !== null && (
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
