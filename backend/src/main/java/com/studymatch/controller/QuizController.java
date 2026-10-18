package com.studymatch.controller;

import com.studymatch.model.Question;
import com.studymatch.model.QuizSubmission;
import com.studymatch.model.QuizSubmissionRequest;
import com.studymatch.service.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST responsável por expor os endpoints HTTP relacionados com o Quiz e Submissões.
 */
@RestController
@RequestMapping("/api")
public class QuizController {

    private final QuizService quizService;

    /**
     * Injeção de dependência do serviço de quiz via construtor.
     *
     * @param quizService Serviço de lógica de negócio do quiz.
     */
    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    /**
     * Endpoint HTTP GET para obter as perguntas do quiz de arranque a frio.
     * Corresponde ao critério de aceitação: GET /api/quiz devolve as perguntas.
     *
     * @return ResponseEntity contendo a lista de perguntas ou o estado HTTP 404 se vazio.
     */
    @GetMapping("/quiz")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Question>> getQuiz() {
        // Por defeito, procuramos as perguntas do quiz inicial com ID 1
        List<Question> questions = quizService.getQuizQuestions(1L);

        // Se não existirem perguntas registadas, retorna 404 Not Found
        if (questions.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Retorna o estado 200 OK juntamente com a lista de perguntas
        return ResponseEntity.ok(questions);
    }

    /**
     * Endpoint HTTP POST para submeter as respostas do quiz por parte de um estudante.
     * Corresponde ao critério de aceitação: POST /api/students/{id}/quiz-submissions guarda as respostas.
     *
     * @param studentId ID do estudante passado no caminho (path variable).
     * @param quizId ID do quiz passado opcionalmente por parâmetro ou assumido por defeito.
     * @param request Objeto contendo o corpo com a lista de respostas dadas pelo estudante.
     * @return ResponseEntity contendo a subscrição/submissão criada.
     */
    @PostMapping("/students/{studentId}/quiz-submissions")
    public ResponseEntity<QuizSubmission> submitQuiz(
            @PathVariable Long studentId,
            @RequestParam(required = false, defaultValue = "1") Long quizId,
            @RequestBody QuizSubmissionRequest request) {

        QuizSubmission submission = quizService.submitQuiz(studentId, quizId, request);
        return ResponseEntity.ok(submission);
    }

    /**
     * Endpoint HTTP GET para obter os resultados do quiz por área (média das 5 respostas)
     * com base na submissão mais recente do estudante (regra de repetição).
     *
     * @param studentId ID do estudante passado no caminho (path variable).
     * @return ResponseEntity contendo um mapa com as competências e as respetivas médias.
     */
    @GetMapping("/students/{studentId}/quiz-results")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Double>> getLatestQuizResults(@PathVariable Long studentId) {
        Map<String, Double> results = quizService.getLatestResultsForStudent(studentId);
        return ResponseEntity.ok(results);
    }
}