import { useEffect, useMemo, useState } from 'react';
import { fetchQuiz, submitQuizSubmission } from '../api/quizApi';
import Egg from '../mascot/Egg.jsx';
import { eggStage } from '../mascot/mascot.js';

const SCALE = [
  { value: 1, label: 'Nada a ver comigo' },
  { value: 2, label: 'Pouco' },
  { value: 3, label: 'Às vezes' },
  { value: 4, label: 'Bastante' },
  { value: 5, label: 'Sou mesmo eu' },
];

/**
 * Quiz inicial no modo estudante: uma pergunta de cada vez, com o ovo a rachar.
 * Não há forma de sair a meio: o Pix só nasce quando o quiz estiver completo.
 */
export default function StudentQuiz({ studentId, onDone }) {
  const [questions, setQuestions] = useState([]);
  const [index, setIndex] = useState(0);
  const [answers, setAnswers] = useState({});
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchQuiz()
      .then((data) => setQuestions(Array.isArray(data) ? data : data?.questions || []))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  const answered = Object.keys(answers).length;
  const total = questions.length;
  const complete = total > 0 && answered === total;
  const question = questions[index];

  // Aviso do browser se tentar fechar a página a meio.
  useEffect(() => {
    if (answered === 0 || submitting) return undefined;
    const warn = (e) => {
      e.preventDefault();
      e.returnValue = '';
    };
    window.addEventListener('beforeunload', warn);
    return () => window.removeEventListener('beforeunload', warn);
  }, [answered, submitting]);

  const stage = useMemo(() => eggStage(answered, total), [answered, total]);

  const choose = (value) => {
    setAnswers((a) => ({ ...a, [question.id]: value }));
    // Avança sozinho para a próxima pergunta por responder.
    if (index < total - 1) setTimeout(() => setIndex((i) => Math.min(i + 1, total - 1)), 220);
  };

  const submit = async () => {
    setSubmitting(true);
    setError(null);
    try {
      const payload = Object.entries(answers).map(([questionId, value]) => ({
        questionId: Number(questionId),
        value,
      }));
      await submitQuizSubmission(studentId, 1, payload);
      onDone();
    } catch (err) {
      setError(err.message || 'Não foi possível guardar as respostas.');
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <main className="sx-center">
        <p className="sx-muted">A preparar o quiz…</p>
      </main>
    );
  }

  if (!question) {
    return (
      <main className="sx-center">
        <p className="sx-error" role="alert">
          {error || 'O quiz ainda não tem perguntas.'}
        </p>
      </main>
    );
  }

  return (
    <main className="sx-quiz">
      <header className="sx-quiz-top">
        <p className="sx-brand">StudyMatch</p>
        <p className="sx-muted">
          {answered} de {total} respondidas
        </p>
      </header>

      <div className="sx-quiz-progress" aria-hidden="true">
        <span style={{ width: `${(answered / total) * 100}%` }} />
      </div>

      <section className="sx-quiz-body">
        <div className="sx-quiz-egg" aria-live="polite">
          <Egg stage={stage} size={150} label={`O ovo está a rachar: ${answered} de ${total}`} />
          <p className="sx-muted sx-small">
            {stage === 0 && 'O Pix está a dormir no ovo…'}
            {stage === 1 && 'Ouviste isto? A casca rachou!'}
            {stage === 2 && 'Está a mexer-se lá dentro!'}
            {stage === 3 && 'Falta pouco!'}
            {stage === 4 && (complete ? 'Está pronto para nascer!' : 'Quase a sair!')}
          </p>
        </div>

        <div className="sx-card sx-question">
          <p className="sx-chip">{question.competency?.name ?? 'Geral'}</p>
          <p className="sx-qcount">
            Pergunta {index + 1} de {total}
          </p>
          <h2>{question.text}</h2>

          <div className="sx-scale" role="radiogroup" aria-label={question.text}>
            {SCALE.map((opt) => (
              <button
                key={opt.value}
                type="button"
                role="radio"
                aria-checked={answers[question.id] === opt.value}
                className={answers[question.id] === opt.value ? 'selected' : ''}
                onClick={() => choose(opt.value)}
              >
                <strong>{opt.value}</strong>
                <span>{opt.label}</span>
              </button>
            ))}
          </div>

          <div className="sx-quiz-nav">
            <button type="button" onClick={() => setIndex((i) => i - 1)} disabled={index === 0}>
              Anterior
            </button>
            {index < total - 1 ? (
              <button
                type="button"
                onClick={() => setIndex((i) => i + 1)}
                disabled={answers[question.id] == null}
              >
                Seguinte
              </button>
            ) : (
              <button
                type="button"
                className="btn-primary sx-big"
                onClick={submit}
                disabled={!complete || submitting}
                title={complete ? undefined : 'Responde a todas as perguntas'}
              >
                {submitting ? 'A chocar…' : 'Chocar o ovo!'}
              </button>
            )}
          </div>

          {!complete && index === total - 1 && (
            <p className="sx-muted sx-small">
              Ainda faltam {total - answered} pergunta(s). Usa “Anterior” para voltar atrás.
            </p>
          )}

          {error && (
            <p className="sx-error" role="alert">
              {error}
            </p>
          )}
        </div>
      </section>
    </main>
  );
}
