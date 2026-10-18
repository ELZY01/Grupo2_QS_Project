package com.studymatch.service;

import com.studymatch.model.Answer;
import com.studymatch.model.Question;
import com.studymatch.model.Quiz;
import com.studymatch.model.QuizSubmission;
import com.studymatch.model.QuizSubmissionRequest;
import com.studymatch.model.Student;
import com.studymatch.repository.AnswerRepository;
import com.studymatch.repository.QuestionRepository;
import com.studymatch.repository.QuizRepository;
import com.studymatch.repository.QuizSubmissionRepository;
import com.studymatch.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Camada de serviço responsável pela lógica de negócio associada aos Quizzes e Submissões.
 */
@Service
@Transactional
public class QuizService {

    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final StudentRepository studentRepository;
    private final AnswerRepository answerRepository;

    /**
     * Injeção de dependências dos repositórios via construtor.
     */
    public QuizService(QuestionRepository questionRepository,
                       QuizRepository quizRepository,
                       QuizSubmissionRepository quizSubmissionRepository,
                       StudentRepository studentRepository,
                       AnswerRepository answerRepository) {
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
        this.quizSubmissionRepository = quizSubmissionRepository;
        this.studentRepository = studentRepository;
        this.answerRepository = answerRepository;
    }

    /**
     * Obtém a lista de todos os quizzes cadastrados.
     *
     * @return Lista com todos os quizzes.
     */
    @Transactional(readOnly = true)
    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    /**
     * Obtém todas as perguntas correspondentes a um determinado quiz,
     * garantindo que vêm devidamente ordenadas.
     *
     * @param quizId O ID do quiz.
     * @return Lista de perguntas do quiz.
     */
    @Transactional(readOnly = true)
    public List<Question> getQuizQuestions(Long quizId) {
        List<Question> questions = questionRepository.findByQuizIdOrderByQuestionOrderAsc(quizId);
        // Força a inicialização da relação com o Quiz acedendo ao ID
        for (Question q : questions) {
            if (q.getQuiz() != null) {
                q.getQuiz().getId();
            }
        }
        return questions;
    }

    /**
     * Regista a submissão de um quiz por parte de um estudante, validando o conjunto de respostas.
     *
     * @param studentId ID do estudante.
     * @param quizId    ID do quiz.
     * @param request   Objeto contendo a lista de respostas dadas pelo estudante.
     * @return A submissão guardada.
     */
    public QuizSubmission submitQuiz(Long studentId, Long quizId, QuizSubmissionRequest request) {
        // Valida se o request e a lista de respostas não são nulos
        if (request == null || request.getAnswers() == null) {
            throw new IllegalArgumentException("O pedido de submissão não pode ser vazio.");
        }

        // Valida a restrição da issue: o quiz deve conter exatamente 20 respostas
        if (request.getAnswers().size() != 20) {
            throw new IllegalArgumentException("O quiz de arranque a frio deve conter exatamente 20 respostas.");
        }

        // Procura o estudante na base de dados
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Estudante não encontrado"));

        // Procura o quiz na base de dados
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz não encontrado"));

        // Cria e guarda a submissão principal
        QuizSubmission submission = new QuizSubmission(student, quiz);
        QuizSubmission savedSubmission = quizSubmissionRepository.save(submission);

        // Processa e guarda cada uma das 20 respostas
        for (QuizSubmissionRequest.AnswerDto answerDto : request.getAnswers()) {
            Question question = questionRepository.findById(answerDto.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Pergunta com ID " + answerDto.getQuestionId() + " não encontrada"));

            // A própria entidade Answer valida se o valor está entre 1 e 5 no método setValue()
            Answer answer = new Answer(savedSubmission, question, answerDto.getValue());
            answerRepository.save(answer);
        }

        return savedSubmission;
    }

    /**
     * Obtém a submissão mais recente de um estudante (regra de repetição: fica o resultado mais recente).
     *
     * @param studentId ID do estudante.
     * @return A submissão mais recente do estudante.
     */
    @Transactional(readOnly = true)
    public QuizSubmission getLatestSubmissionForStudent(Long studentId) {
        return quizSubmissionRepository.findTopByStudentIdOrderBySubmittedAtDesc(studentId)
                .orElseThrow(() -> new RuntimeException("Nenhuma submissão encontrada para o estudante com ID " + studentId));
    }

    /**
     * Calcula o resultado por área (competência) para uma submissão,
     * calculando a média aritmética dos valores das 5 respostas dessa área (1-5).
     *
     * @param submissionId ID da submissão.
     * @return Mapa contendo o nome da competência e a respetiva média.
     */
    @Transactional(readOnly = true)
    public Map<String, Double> calculateResultsForSubmission(Long submissionId) {
        List<Answer> answers = answerRepository.findBySubmissionId(submissionId);

        // Agrupa os valores das respostas por nome/código da competência associada à pergunta
        Map<String, List<Integer>> competencyValues = new HashMap<>();
        for (Answer answer : answers) {
            String competencyName = answer.getQuestion().getCompetency().getName();
            competencyValues.computeIfAbsent(competencyName, k -> new ArrayList<>()).add(answer.getValue());
        }

        // Calcula a média para cada competência
        Map<String, Double> competencyAverages = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : competencyValues.entrySet()) {
            List<Integer> values = entry.getValue();
            double average = values.stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);
            competencyAverages.put(entry.getKey(), average);
        }

        return competencyAverages;
    }

    /**
     * Obtém os resultados do quiz para a submissão mais recente do estudante.
     *
     * @param studentId ID do estudante.
     * @return Mapa com o resultado por área (média das 5 respostas dessa área).
     */
    @Transactional(readOnly = true)
    public Map<String, Double> getLatestResultsForStudent(Long studentId) {
        QuizSubmission latestSubmission = getLatestSubmissionForStudent(studentId);
        return calculateResultsForSubmission(latestSubmission.getId());
    }
}