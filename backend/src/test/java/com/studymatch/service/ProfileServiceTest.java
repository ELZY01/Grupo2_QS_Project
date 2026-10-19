package com.studymatch.service;

import com.studymatch.dto.ProfileResponse;
import com.studymatch.dto.ProfileResponse.CompetencyProfile;
import com.studymatch.model.Answer;
import com.studymatch.model.Competency;
import com.studymatch.model.CourseUnit;
import com.studymatch.model.CourseUnitCompetency;
import com.studymatch.model.Enrollment;
import com.studymatch.model.Enrollment.EnrollmentStatus;
import com.studymatch.model.Question;
import com.studymatch.model.Quiz;
import com.studymatch.model.QuizSubmission;
import com.studymatch.model.Student;
import com.studymatch.profile.LevelOrigin;
import com.studymatch.repository.AnswerRepository;
import com.studymatch.repository.CompetencyRepository;
import com.studymatch.repository.CourseUnitCompetencyRepository;
import com.studymatch.repository.EnrollmentRepository;
import com.studymatch.repository.QuizSubmissionRepository;
import com.studymatch.repository.StudentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Testes do ProfileService (Issue #45): confirma que os dados da base de dados
 * chegam corretamente ao ProfileCalculator. As regras em si estão testadas
 * em ProfileCalculatorTest; aqui usamos Mockito para simular os repositórios.
 */
@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    private static final double EPS = 1e-9;
    private static final Long STUDENT_ID = 1L;

    @Mock private StudentRepository studentRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private CompetencyRepository competencyRepository;
    @Mock private CourseUnitCompetencyRepository courseUnitCompetencyRepository;
    @Mock private QuizSubmissionRepository quizSubmissionRepository;
    @Mock private AnswerRepository answerRepository;

    @InjectMocks
    private ProfileService profileService;

    private Student student;
    private Competency prog;
    private Competency equipa;
    private CourseUnit prog1;
    private CourseUnit poo;

    @BeforeEach
    void setUp() {
        student = new Student("a001", "Ana Silva", "ana@upt.pt", "Eng. Informática", 1);
        prog = new Competency("PROG", "Programação", "Programar", true);
        equipa = new Competency("EQUIPA", "Trabalho em grupo", "Trabalhar em equipa", true);
        prog1 = new CourseUnit("PROG1", "Programação I", 6, 1, 1);
        poo = new CourseUnit("POO", "Programação Orientada a Objetos", 6, 1, 2);
    }

    /** Estudante existente, 2 competências e os pesos PROG1/POO → PROG. */
    private void givenStudentAndCompetencies() {
        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));
        when(competencyRepository.findAllByOrderByIdAsc()).thenReturn(List.of(prog, equipa));
        when(courseUnitCompetencyRepository.findAllWithCourseUnitAndCompetency()).thenReturn(List.of(
                new CourseUnitCompetency(prog1, prog, 1.0),
                new CourseUnitCompetency(poo, prog, 1.0)));
    }

    private Enrollment enrollment(CourseUnit unit, int attempt, Integer grade, EnrollmentStatus status) {
        return new Enrollment(student, unit, 2025, 1, attempt, grade, status);
    }

    private static CompetencyProfile competency(ProfileResponse response, String code) {
        return response.competencies().stream()
                .filter(c -> c.code().equals(code))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void estudanteInexistenteDevolve404() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> profileService.getProfile(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void perfilComHistoricoUsaAMelhorNotaDeCadaUc() {
        givenStudentAndCompetencies();
        when(enrollmentRepository.findByStudentIdOrderByCourseUnitIdAscAttemptNumberAsc(STUDENT_ID))
                .thenReturn(List.of(
                        enrollment(prog1, 1, 8, EnrollmentStatus.REPROVADO),
                        enrollment(prog1, 2, 20, EnrollmentStatus.APROVADO),
                        enrollment(poo, 1, 10, EnrollmentStatus.APROVADO)));
        when(quizSubmissionRepository.findTopByStudentIdOrderBySubmittedAtDesc(STUDENT_ID))
                .thenReturn(Optional.empty());

        ProfileResponse response = profileService.getProfile(STUDENT_ID);

        CompetencyProfile progLevel = competency(response, "PROG");
        assertEquals("Ana Silva", response.studentName());
        assertEquals(2, response.completedCourseUnits());
        assertFalse(response.quizCompleted());
        assertEquals(LevelOrigin.HISTORY, progLevel.origin());
        assertEquals((5.0 + 1.0) / 2, progLevel.level(), EPS);   // PROG1 = 20 → 5; POO = 10 → 1
        assertEquals("Programação", progLevel.name());
        assertEquals(2, progLevel.contributions().size());
    }

    @Test
    void alunoSoComQuizTemPerfilPelasMediasDeCadaArea() {
        givenStudentAndCompetencies();
        when(enrollmentRepository.findByStudentIdOrderByCourseUnitIdAscAttemptNumberAsc(STUDENT_ID))
                .thenReturn(List.of());

        Quiz quiz = new Quiz("v1");
        QuizSubmission submission = new QuizSubmission(student, quiz);
        List<Answer> answers = new ArrayList<>();
        int[] progValues = {5, 4, 4, 3, 4};     // média 4,0
        int[] equipaValues = {2, 2, 3, 2, 1};   // média 2,0
        for (int i = 0; i < 5; i++) {
            answers.add(new Answer(submission, new Question(quiz, prog, "P" + i, i + 1), progValues[i]));
            answers.add(new Answer(submission, new Question(quiz, equipa, "E" + i, i + 6), equipaValues[i]));
        }
        when(quizSubmissionRepository.findTopByStudentIdOrderBySubmittedAtDesc(STUDENT_ID))
                .thenReturn(Optional.of(submission));
        when(answerRepository.findBySubmissionId(any())).thenReturn(answers);

        ProfileResponse response = profileService.getProfile(STUDENT_ID);

        assertTrue(response.quizCompleted());
        assertEquals(1.0, response.quizWeight(), EPS);
        assertEquals(LevelOrigin.QUIZ, competency(response, "PROG").origin());
        assertEquals(4.0, competency(response, "PROG").level(), EPS);
        assertEquals(2.0, competency(response, "EQUIPA").level(), EPS);
    }

    @Test
    void alunoSemQuizNemNotasFicaSemDados() {
        givenStudentAndCompetencies();
        when(enrollmentRepository.findByStudentIdOrderByCourseUnitIdAscAttemptNumberAsc(STUDENT_ID))
                .thenReturn(List.of(enrollment(prog1, 1, null, EnrollmentStatus.EM_CURSO)));
        when(quizSubmissionRepository.findTopByStudentIdOrderBySubmittedAtDesc(STUDENT_ID))
                .thenReturn(Optional.empty());

        ProfileResponse response = profileService.getProfile(STUDENT_ID);

        assertEquals(0, response.completedCourseUnits());
        for (CompetencyProfile c : response.competencies()) {
            assertEquals(LevelOrigin.NO_DATA, c.origin());
            assertNull(c.level());
        }
    }
}
