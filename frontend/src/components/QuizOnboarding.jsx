import React, { useEffect, useState } from 'react';
import { fetchQuiz, submitQuizSubmission } from '../api/quizApi';
import './QuizOnboarding.css';

export const QuizOnboarding = ({ studentId = 1, onComplete }) => {
    const [quiz, setQuiz] = useState([]); // Agora inicializado como array
    const [answers, setAnswers] = useState({}); // { questionId: rating }
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [unanswered, setUnanswered] = useState([]);
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        fetchQuiz()
            .then((data) => {
                setQuiz(data);
                setLoading(false);
            })
            .catch((err) => {
                setError(err.message);
                setLoading(false);
            });
    }, []);

    const handleSelectValue = (questionId, value) => {
        setAnswers((prev) => ({
            ...prev,
            [questionId]: value
        }));
        // Limpa o destaque de não respondida quando o utilizador escolhe um valor
        setUnanswered((prev) => prev.filter((id) => id !== questionId));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const questionsArray = Array.isArray(quiz) ? quiz : (quiz?.questions || []);
        if (questionsArray.length === 0) return;

        // Validar se todas as perguntas têm resposta
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
            // ID do quiz inicial por defeito é 1
            await submitQuizSubmission(studentId, 1, payloadAnswers);
            if (onComplete) {
                onComplete(); // Redireciona / navega para o perfil do estudante
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

    // Agrupar perguntas por área / competência suportando array direto
    const questionsArray = Array.isArray(quiz) ? quiz : (quiz?.questions || []);
    const groupedQuestions = questionsArray.reduce((acc, q) => {
        const compName = q.competency?.name || 'Geral';
        if (!acc[compName]) acc[compName] = [];
        acc[compName].push(q);
        return acc;
    }, {});

    return (
        <div className="quiz-container">
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