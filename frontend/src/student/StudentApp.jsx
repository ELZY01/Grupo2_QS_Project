import { useEffect, useState } from 'react';
import { getProfile } from '../api/profileApi';
import Hatch from '../mascot/Hatch.jsx';
import StudentHome from './StudentHome.jsx';
import StudentQuiz from './StudentQuiz.jsx';
import './student.css';

/**
 * Área do estudante com sessão iniciada.
 * Quem ainda não fez o quiz inicial vai sempre parar ao quiz (o Pix está no ovo):
 * só depois de o Pix nascer é que aparece a área do estudante.
 */
export default function StudentApp({ studentId, startWithQuiz = false, onLogout }) {
  const [step, setStep] = useState(startWithQuiz ? 'quiz' : 'checking');
  const [error, setError] = useState(null);

  useEffect(() => {
    if (step !== 'checking') return undefined;
    let alive = true;
    getProfile(studentId)
      .then((p) => alive && setStep(p.quizCompleted ? 'home' : 'quiz'))
      .catch((err) => alive && setError(err.message));
    return () => {
      alive = false;
    };
  }, [step, studentId]);

  return (
    <div className="sx">
      {step === 'checking' && (
        <main className="sx-center">
          {error ? (
            <div className="sx-card sx-form">
              <p className="sx-error" role="alert">
                Não foi possível abrir a tua conta ({error}).
              </p>
              <button type="button" onClick={onLogout}>
                Voltar ao início
              </button>
            </div>
          ) : (
            <p className="sx-muted">A acordar o Pix…</p>
          )}
        </main>
      )}

      {step === 'quiz' && <StudentQuiz studentId={studentId} onDone={() => setStep('hatch')} />}

      {step === 'hatch' && (
        <main className="sx-center">
          <Hatch studentId={studentId} viewLabel="Conhecer o meu Pix" onViewProfile={() => setStep('home')} />
        </main>
      )}

      {step === 'home' && <StudentHome studentId={studentId} onLogout={onLogout} />}
    </div>
  );
}
