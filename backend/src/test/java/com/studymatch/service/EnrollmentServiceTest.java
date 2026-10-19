
package com.studymatch.service;

import com.studymatch.dto.EnrollmentRequest;
import com.studymatch.dto.EnrollmentResponse;
import com.studymatch.model.CourseUnit;
import com.studymatch.model.Enrollment;
import com.studymatch.model.Enrollment.EnrollmentStatus;
import com.studymatch.model.Student;
import com.studymatch.repository.CourseUnitRepository;
import com.studymatch.repository.EnrollmentRepository;
import com.studymatch.repository.StudentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários do serviço de inscrições académicas.
 *
 * Utilizamos Mockito para simular os repositórios.
 * Assim, podemos testar as regras de negócio sem
 * estabelecer uma ligação à base de dados Supabase.
 */
@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    // Simula o repositório das inscrições.
    @Mock
    private EnrollmentRepository enrollmentRepository;

    // Simula o repositório dos estudantes.
    @Mock
    private StudentRepository studentRepository;

    // Simula o repositório das unidades curriculares.
    @Mock
    private CourseUnitRepository courseUnitRepository;

    // Cria o serviço e injeta os repositórios simulados.
    @InjectMocks
    private EnrollmentService enrollmentService;

    private Student student;
    private CourseUnit courseUnit;

    /**
     * Executado antes de cada teste.
     *
     * Cria um estudante e uma unidade curricular simulados.
     * Define também os respetivos identificadores.
     */
    @BeforeEach
    void setUp() {

        student = mock(Student.class);
        courseUnit = mock(CourseUnit.class);

        // lenient evita avisos quando um teste não utiliza estes métodos.
        lenient().when(student.getId()).thenReturn(1L);
        lenient().when(courseUnit.getId()).thenReturn(2L);
    }

    /**
     * Prepara os repositórios para uma inscrição válida.
     *
     * Simula a existência do estudante e da UC,
     * as tentativas anteriores e a gravação da inscrição.
     */
    private void prepareSuccessfulCreation(List<Enrollment> attempts) {

        when(studentRepository.findById(1L))
                .thenReturn(Optional.of(student));

        when(courseUnitRepository.findById(2L))
                .thenReturn(Optional.of(courseUnit));

        when(enrollmentRepository
                .findByStudentIdAndCourseUnitIdOrderByAttemptNumberDesc(1L, 2L))
                .thenReturn(attempts);

        // Devolve a inscrição recebida, simulando o método save().
        when(enrollmentRepository.save(any(Enrollment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    /**
     * TESTE 1
     * Verifica se a nota 0 é aceite para um estudante reprovado.
     */
    @Test
    void shouldAcceptGradeZero() {

        prepareSuccessfulCreation(List.of());

        EnrollmentRequest request = new EnrollmentRequest(
                2L,
                2026,
                1,
                0,
                EnrollmentStatus.REPROVADO
        );

        EnrollmentResponse response =
                enrollmentService.createEnrollment(1L, request);

        assertEquals(0, response.grade());
        assertEquals(1, response.attemptNumber());
        assertEquals(EnrollmentStatus.REPROVADO, response.status());
    }

    /**
     * TESTE 2
     * Verifica se a nota máxima de 20 é aceite.
     */
    @Test
    void shouldAcceptGradeTwenty() {

        prepareSuccessfulCreation(List.of());

        EnrollmentRequest request = new EnrollmentRequest(
                2L,
                2026,
                1,
                20,
                EnrollmentStatus.APROVADO
        );

        EnrollmentResponse response =
                enrollmentService.createEnrollment(1L, request);

        assertEquals(20, response.grade());
        assertEquals(EnrollmentStatus.APROVADO, response.status());
    }

    /**
     * TESTE 3
     * Uma nota negativa deve provocar um erro HTTP 400.
     */
    @Test
    void shouldRejectNegativeGrade() {

        EnrollmentRequest request = new EnrollmentRequest(
                2L,
                2026,
                1,
                -1,
                EnrollmentStatus.REPROVADO
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> enrollmentService.createEnrollment(1L, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        // Confirma que nenhuma inscrição foi guardada.
        verify(enrollmentRepository, never()).save(any());
    }

    /**
     * TESTE 4
     * Uma nota superior a 20 deve provocar um erro HTTP 400.
     */
    @Test
    void shouldRejectGradeAboveTwenty() {

        EnrollmentRequest request = new EnrollmentRequest(
                2L,
                2026,
                1,
                21,
                EnrollmentStatus.APROVADO
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> enrollmentService.createEnrollment(1L, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    /**
     * TESTE 5
     * Uma inscrição EM_CURSO pode ter nota null.
     */
    @Test
    void shouldAcceptEnrollmentInProgress() {

        prepareSuccessfulCreation(List.of());

        EnrollmentRequest request = new EnrollmentRequest(
                2L,
                2026,
                1,
                null,
                EnrollmentStatus.EM_CURSO
        );

        EnrollmentResponse response =
                enrollmentService.createEnrollment(1L, request);

        assertNull(response.grade());
        assertEquals(EnrollmentStatus.EM_CURSO, response.status());
    }

    /**
     * TESTE 6
     * Uma inscrição EM_CURSO não pode ter nota atribuída.
     */
    @Test
    void shouldRejectGradeWhenInProgress() {

        EnrollmentRequest request = new EnrollmentRequest(
                2L,
                2026,
                1,
                15,
                EnrollmentStatus.EM_CURSO
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> enrollmentService.createEnrollment(1L, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    /**
     * TESTE 7
     * Verifica se o número da tentativa aumenta automaticamente.
     *
     * Se a última tentativa foi a 2,
     * a nova inscrição deve receber o número 3.
     */
    @Test
    void shouldIncrementAttemptNumber() {

        Enrollment previous = new Enrollment(
                student,
                courseUnit,
                2025,
                2,
                2,
                8,
                EnrollmentStatus.REPROVADO
        );

        prepareSuccessfulCreation(List.of(previous));

        EnrollmentRequest request = new EnrollmentRequest(
                2L,
                2026,
                1,
                12,
                EnrollmentStatus.APROVADO
        );

        EnrollmentResponse response =
                enrollmentService.createEnrollment(1L, request);

        assertEquals(3, response.attemptNumber());
    }

    /**
     * TESTE 8
     * Não é possível inscrever um estudante inexistente.
     */
    @Test
    void shouldRejectUnknownStudent() {

        when(studentRepository.findById(999L))
                .thenReturn(Optional.empty());

        EnrollmentRequest request = new EnrollmentRequest(
                2L,
                2026,
                1,
                15,
                EnrollmentStatus.APROVADO
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> enrollmentService.createEnrollment(999L, request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(enrollmentRepository, never()).save(any());
    }

    /**
     * TESTE 9
     * Verifica se o histórico académico é devolvido corretamente.
     */
    @Test
    void shouldReturnStudentHistory() {

        when(studentRepository.existsById(1L))
                .thenReturn(true);

        Enrollment enrollment = new Enrollment(
                student,
                courseUnit,
                2026,
                1,
                1,
                16,
                EnrollmentStatus.APROVADO
        );

        when(enrollmentRepository
                .findByStudentIdOrderByCourseUnitIdAscAttemptNumberAsc(1L))
                .thenReturn(List.of(enrollment));

        List<EnrollmentResponse> history =
                enrollmentService.getStudentEnrollments(1L);

        assertEquals(1, history.size());
        assertEquals(16, history.get(0).grade());
        assertEquals(EnrollmentStatus.APROVADO, history.get(0).status());
    }
}
