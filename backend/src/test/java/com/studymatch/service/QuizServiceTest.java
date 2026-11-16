package com.studymatch.service;

import com.studymatch.model.*;
import com.studymatch.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class QuizServiceTest {

    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private QuizRepository quizRepository;
    @Mock
    private QuizSubmissionRepository quizSubmissionRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private AnswerRepository answerRepository;

    @InjectMocks
    private QuizService quizService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSubmitQuiz_NormalSubmission() {
        Long studentId = 1L;
        Long quizId = 1L;
        Student student = new Student();
        Quiz quiz = new Quiz();
        QuizSubmission submission = new QuizSubmission(student, quiz);

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
        when(quizSubmissionRepository.save(any(QuizSubmission.class))).thenReturn(submission);

        Competency comp = new Competency();
        comp.setName("Programação");

        QuizSubmissionRequest request = new QuizSubmissionRequest();
        List<QuizSubmissionRequest.AnswerDto> answers = new ArrayList<>();

        for (long i = 7; i <= 26; i++) {
            QuizSubmissionRequest.AnswerDto dto = new QuizSubmissionRequest.AnswerDto();
            dto.setQuestionId(i);
            dto.setValue(4);
            answers.add(dto);

            Question q = mock(Question.class);
            when(q.getId()).thenReturn(i);
            when(q.getCompetency()).thenReturn(comp);
            when(questionRepository.findById(i)).thenReturn(Optional.of(q));
        }
        request.setAnswers(answers);

        QuizSubmission result = quizService.submitQuiz(studentId, quizId, request);

        assertNotNull(result);
        verify(quizSubmissionRepository, times(1)).save(any(QuizSubmission.class));
        verify(answerRepository, times(20)).save(any(Answer.class));
    }

    @Test
    void testSubmitQuiz_InvalidAnswerCount_ThrowsException() {
        Long studentId = 1L;
        Long quizId = 1L;
        QuizSubmissionRequest request = new QuizSubmissionRequest();
        List<QuizSubmissionRequest.AnswerDto> answers = new ArrayList<>();

        for (long i = 7; i <= 25; i++) {
            QuizSubmissionRequest.AnswerDto dto = new QuizSubmissionRequest.AnswerDto();
            dto.setQuestionId(i);
            dto.setValue(3);
            answers.add(dto);
        }
        request.setAnswers(answers);

        // Atualizado para esperar a ResponseStatusException e a mensagem formatada do Spring
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            quizService.submitQuiz(studentId, quizId, request);
        });

        assertEquals("400 BAD_REQUEST \"O quiz de arranque a frio deve conter exatamente 20 respostas.\"", exception.getMessage());
    }

    @Test
    void testSubmitQuiz_BoundaryValues_AllOnes() {
        Long studentId = 1L;
        Long quizId = 1L;
        Student student = new Student();
        Quiz quiz = new Quiz();
        QuizSubmission submission = new QuizSubmission(student, quiz);

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
        when(quizSubmissionRepository.save(any(QuizSubmission.class))).thenReturn(submission);

        Competency comp = new Competency();
        comp.setName("Organização");

        QuizSubmissionRequest request = new QuizSubmissionRequest();
        List<QuizSubmissionRequest.AnswerDto> answers = new ArrayList<>();

        for (long i = 7; i <= 26; i++) {
            QuizSubmissionRequest.AnswerDto dto = new QuizSubmissionRequest.AnswerDto();
            dto.setQuestionId(i);
            dto.setValue(1);
            answers.add(dto);

            Question q = mock(Question.class);
            when(q.getId()).thenReturn(i);
            when(q.getCompetency()).thenReturn(comp);
            when(questionRepository.findById(i)).thenReturn(Optional.of(q));
        }
        request.setAnswers(answers);

        QuizSubmission result = quizService.submitQuiz(studentId, quizId, request);

        assertNotNull(result);
        verify(answerRepository, times(20)).save(any(Answer.class));
    }

    @Test
    void testSubmitQuiz_BoundaryValues_AllFives() {
        Long studentId = 1L;
        Long quizId = 1L;
        Student student = new Student();
        Quiz quiz = new Quiz();
        QuizSubmission submission = new QuizSubmission(student, quiz);

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
        when(quizSubmissionRepository.save(any(QuizSubmission.class))).thenReturn(submission);

        Competency comp = new Competency();
        comp.setName("Gestão de tempo");

        QuizSubmissionRequest request = new QuizSubmissionRequest();
        List<QuizSubmissionRequest.AnswerDto> answers = new ArrayList<>();

        for (long i = 7; i <= 26; i++) {
            QuizSubmissionRequest.AnswerDto dto = new QuizSubmissionRequest.AnswerDto();
            dto.setQuestionId(i);
            dto.setValue(5);
            answers.add(dto);

            Question q = mock(Question.class);
            when(q.getId()).thenReturn(i);
            when(q.getCompetency()).thenReturn(comp);
            when(questionRepository.findById(i)).thenReturn(Optional.of(q));
        }
        request.setAnswers(answers);

        QuizSubmission result = quizService.submitQuiz(studentId, quizId, request);

        assertNotNull(result);
        verify(answerRepository, times(20)).save(any(Answer.class));
    }

    // ---------------------------------------------------------------
    // Submissão: caminhos de erro
    // ---------------------------------------------------------------

    /** Cria um pedido com 20 respostas (perguntas 7 a 26), todas com o mesmo valor. */
    private QuizSubmissionRequest pedidoCom20Respostas(int valor) {
        QuizSubmissionRequest request = new QuizSubmissionRequest();
        List<QuizSubmissionRequest.AnswerDto> answers = new ArrayList<>();
        for (long i = 7; i <= 26; i++) {
            QuizSubmissionRequest.AnswerDto dto = new QuizSubmissionRequest.AnswerDto();
            dto.setQuestionId(i);
            dto.setValue(valor);
            answers.add(dto);
        }
        request.setAnswers(answers);
        return request;
    }

    /** Prepara um aluno e um quiz existentes, com a submissão a ser guardada. */
    private void alunoEQuizExistem() {
        Student student = new Student();
        Quiz quiz = new Quiz();
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(quizSubmissionRepository.save(any(QuizSubmission.class)))
                .thenReturn(new QuizSubmission(student, quiz));
    }

    @Test
    void submitQuiz_PedidoNulo_Devolve400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizService.submitQuiz(1L, 1L, null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verifyNoInteractions(quizSubmissionRepository, answerRepository);
    }

    @Test
    void submitQuiz_ListaDeRespostasNula_Devolve400() {
        QuizSubmissionRequest request = new QuizSubmissionRequest();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizService.submitQuiz(1L, 1L, request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verifyNoInteractions(quizSubmissionRepository, answerRepository);
    }

    @Test
    void submitQuiz_RespostasAMais_Devolve400() {
        QuizSubmissionRequest request = pedidoCom20Respostas(3);
        QuizSubmissionRequest.AnswerDto extra = new QuizSubmissionRequest.AnswerDto();
        extra.setQuestionId(27L);
        extra.setValue(3);
        request.getAnswers().add(extra);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizService.submitQuiz(1L, 1L, request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verifyNoInteractions(quizSubmissionRepository, answerRepository);
    }

    @Test
    void submitQuiz_AlunoInexistente_Devolve404() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizService.submitQuiz(99L, 1L, pedidoCom20Respostas(3)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Estudante não encontrado", ex.getReason());
        verify(quizSubmissionRepository, never()).save(any());
    }

    @Test
    void submitQuiz_QuizInexistente_Devolve404() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(new Student()));
        when(quizRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizService.submitQuiz(1L, 99L, pedidoCom20Respostas(3)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Quiz não encontrado", ex.getReason());
        verify(quizSubmissionRepository, never()).save(any());
    }

    @Test
    void submitQuiz_PerguntaInexistente_Devolve400ComOId() {
        alunoEQuizExistem();
        when(questionRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizService.submitQuiz(1L, 1L, pedidoCom20Respostas(3)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Pergunta com ID 7 não encontrada", ex.getReason());
        verify(answerRepository, never()).save(any());
    }

    @Test
    void submitQuiz_ValorForaDaEscala_Devolve400() {
        alunoEQuizExistem();
        when(questionRepository.findById(anyLong())).thenReturn(Optional.of(new Question()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizService.submitQuiz(1L, 1L, pedidoCom20Respostas(6)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(answerRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // Consulta de quizzes e perguntas
    // ---------------------------------------------------------------

    @Test
    void getAllQuizzes_DevolveOsQuizzesDoRepositorio() {
        List<Quiz> quizzes = List.of(new Quiz(), new Quiz());
        when(quizRepository.findAll()).thenReturn(quizzes);

        assertEquals(quizzes, quizService.getAllQuizzes());
    }

    @Test
    void getQuizQuestions_QuizInexistente_Devolve404() {
        when(quizRepository.existsById(99L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> quizService.getQuizQuestions(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verifyNoInteractions(questionRepository);
    }

    @Test
    void getQuizQuestions_DevolvePerguntasPorOrdem() {
        Question comQuiz = new Question();
        comQuiz.setQuiz(new Quiz());
        Question semQuiz = new Question();
        List<Question> perguntas = List.of(comQuiz, semQuiz);

        when(quizRepository.existsById(1L)).thenReturn(true);
        when(questionRepository.findByQuizIdOrderByQuestionOrderAsc(1L)).thenReturn(perguntas);

        assertEquals(perguntas, quizService.getQuizQuestions(1L));
    }
}
