import React, { useEffect, useState } from 'react';
import { fetchQuiz, submitQuizSubmission } from '../api/quizApi';
import { getStudents } from '../api/studentApi';
import './QuizOnboarding.css';

export const QuizOnboarding = ({ studentId, onSelectStudent, onComplete }) => {
    const [students, setStudents] = useState([]);
    const [quiz, setQuiz] = useState([]);
    const [answers, setAnswers] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [unanswered, setUnanswered] = useState([]);
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        const loadData = async () => {
            try {
                const [quizData, studentsData] = await Promise.all([
                    fetchQuiz(),
                    getStudents()
                ]);
                setQuiz(quizData);
                setStudents(studentsData);

                // Se não houver studentId definido ou se o atual for 1 por defeito sem existir, escolhe o primeiro da lista
                if (!studentId && studentsData.length > 0) {
                    if (onSelectStudent) onSelectStudent(studentsData[0].id);
                }
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        loadData();
    }, []);

    const handleSelectValue = (questionId, value) => {
        setAnswers((prev) => ({
            ...prev,
            [questionId]: value
        }));
        setUnanswered((prev) => prev.filter((id) => id !== questionId));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        const activeStudentId = studentId || (students.length > 0 ? students[0].id : null);
        if (!activeStudentId) {
            setError('Por favor, seleciona um estudante antes de submeter.');
            return;
        }

        const questionsArray = Array.isArray(quiz) ? quiz : (quiz?.questions || []);
        if (questionsArray.length === 0) return;

        const missing = questionsArray
            .filter((q) => !answers[q.id])
            .map((q) => q.id);

        if (missing.length > 0) {
            setUnanswered(missing);
            setError(`Faltam responder a ${missing.length} pergunta(s). Assinaladas abaixo.`);
            return;
        }

        setError(null);
        setSubmitting(true);

        const payloadAnswers = Object.entries(answers).map(([qId, val]) => ({
            questionId: Number(qId),
            value: val
        }));

        try {
            await submitQuizSubmission(activeStudentId, 1, payloadAnswers);
            if (onComplete) {
                onComplete();
            } else {
                alert('Quiz submetido com sucesso!');
            }
        } catch (err) {
            setError(err.message || 'Erro ao submeter o quiz.');
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) return <div className="quiz-container">A carregar o questionário...</div>;
    if (error && (!quiz || quiz.length === 0)) return <div className="quiz-container error">{error}</div>;

    const questionsArray = Array.isArray(quiz) ? quiz : (quiz?.questions || []);
    const groupedQuestions = questionsArray.reduce((acc, q) => {
        const compName = q.competency?.name || 'Geral';
        if (!acc[compName]) acc[compName] = [];
        acc[compName].push(q);
        return acc;
    }, {});

    const activeStudentId = studentId || (students.length > 0 ? students[0].id : '');

    return (
        <div className="quiz-container">
            <div className="student-selector-box" style={{ marginBottom: '1.5rem' }}>
                <label htmlFor="quiz-student-select" style={{ fontWeight: '600', marginRight: '0.5rem' }}>
                    Estudante:
                </label>
                <select
                    id="quiz-student-select"
                    value={activeStudentId}
                    onChange={(e) => onSelectStudent && onSelectStudent(Number(e.target.value))}
                    className="input"
                    style={{ padding: '0.4rem 0.8rem', borderRadius: '6px', border: '1px solid #ccc' }}
                >
                    {students.map((s) => (
                        <option key={s.id} value={s.id}>
                            {s.number} · {s.name}
                        </option>
                    ))}
                </select>
            </div>

            <h2>Questionário Inicial de Competências</h2>
            <p>Responde às seguintes perguntas numa escala de 1 (Discordo Totalmente) a 5 (Concordo Totalmente).</p>

            {error && <div className="quiz-alert-error">{error}</div>}

            <form onSubmit={handleSubmit}>
                {Object.entries(groupedQuestions).map(([competencyName, questions]) => (
                    <div key={competencyName} className="quiz-group">
                        <h3>{competencyName}</h3>
                        {questions.map((q) => {
                            const isMissing = unanswered.includes(q.id);
                            return (
                                <div key={q.id} className={`quiz-question-card ${isMissing ? 'missing' : ''}`}>
                                    <p className="question-text">{q.text}</p>
                                    {isMissing && <span className="missing-label">Resposta obrigatória</span>}
                                    <div className="rating-options">
                                        {[1, 2, 3, 4, 5].map((val) => (
                                            <label key={val} className={`rating-btn ${answers[q.id] === val ? 'selected' : ''}`}>
                                                <input
                                                    type="radio"
                                                    name={`question-${q.id}`}
                                                    value={val}
                                                    checked={answers[q.id] === val}
                                                    onChange={() => handleSelectValue(q.id, val)}
                                                />
                                                {val}
                                            </label>
                                        ))}
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                ))}

                <button type="submit" className="submit-btn" disabled={submitting}>
                    {submitting ? 'A submeter...' : 'Submeter e Ir para o Perfil'}
                </button>
            </form>
        </div>
    );
};