package com.studymatch.service;

import com.studymatch.model.*;
import com.studymatch.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            quizService.submitQuiz(studentId, quizId, request);
        });

        assertEquals("O quiz de arranque a frio deve conter exatamente 20 respostas.", exception.getMessage());
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
}