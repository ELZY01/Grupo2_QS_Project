
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

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Contém as regras de negócio das inscrições académicas.
 *
 * Verifica os dados, consulta a base de dados
 * e calcula automaticamente o número da tentativa.
 */
@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseUnitRepository courseUnitRepository;

    // O Spring fornece automaticamente os três repositórios.
    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            StudentRepository studentRepository,
            CourseUnitRepository courseUnitRepository) {

        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseUnitRepository = courseUnitRepository;
    }

    /**
     * Regista uma nova inscrição numa unidade curricular.
     */
    @Transactional
    public EnrollmentResponse createEnrollment(
            Long studentId,
            EnrollmentRequest request) {

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Os dados da inscrição são obrigatórios."
            );
        }

        validateEnrollment(request);

        // Verifica se o estudante existe.
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Estudante não encontrado."
                ));

        // Verifica se a unidade curricular existe.
        CourseUnit courseUnit = courseUnitRepository
                .findById(request.courseUnitId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unidade curricular não encontrada."
                ));

        // Consulta as tentativas anteriores do estudante nesta UC.
        List<Enrollment> previousAttempts =
                enrollmentRepository
                        .findByStudentIdAndCourseUnitIdOrderByAttemptNumberDesc(
                                studentId,
                                request.courseUnitId()
                        );

        // A primeira inscrição é a tentativa 1.
        // As seguintes recebem o número anterior + 1.
        int nextAttempt = previousAttempts.isEmpty()
                ? 1
                : previousAttempts.get(0).getAttemptNumber() + 1;

        Enrollment enrollment = new Enrollment(
                student,
                courseUnit,
                request.schoolYear(),
                request.semester(),
                nextAttempt,
                request.grade(),
                request.status()
        );

        Enrollment saved = enrollmentRepository.save(enrollment);

        return EnrollmentResponse.from(saved);
    }

    /**
     * Consulta o histórico académico de um estudante.
     */
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getStudentEnrollments(Long studentId) {

        if (!studentRepository.existsById(studentId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Estudante não encontrado."
            );
        }

        return enrollmentRepository
                .findByStudentIdOrderByCourseUnitIdAscAttemptNumberAsc(studentId)
                .stream()
                .map(EnrollmentResponse::from)
                .toList();
    }

    /**
     * Valida os dados recebidos antes de guardar a inscrição.
     */
    private void validateEnrollment(EnrollmentRequest request) {

        if (request.courseUnitId() == null) {
            badRequest("A unidade curricular é obrigatória.");
        }

        if (request.schoolYear() == null
                || request.schoolYear() < 2000) {
            badRequest("O ano letivo é inválido.");
        }

        if (request.semester() == null
                || request.semester() < 1
                || request.semester() > 2) {
            badRequest("O semestre deve ser 1 ou 2.");
        }

        if (request.status() == null) {
            badRequest("O estado da inscrição é obrigatório.");
        }

        // Uma inscrição em curso não pode ter nota.
        if (request.status() == EnrollmentStatus.EM_CURSO) {
            if (request.grade() != null) {
                badRequest("Uma inscrição em curso não pode ter nota.");
            }
            return;
        }

        // Inscrições concluídas exigem nota entre 0 e 20.
        if (request.grade() == null
                || request.grade() < 0
                || request.grade() > 20) {
            badRequest("A nota deve estar entre 0 e 20.");
        }

        // Uma nota de 10 ou mais corresponde a aprovação.
        if (request.status() == EnrollmentStatus.APROVADO
                && request.grade() < 10) {
            badRequest("Uma inscrição aprovada deve ter nota de 10 a 20.");
        }

        if (request.status() == EnrollmentStatus.REPROVADO
                && request.grade() >= 10) {
            badRequest("Uma inscrição reprovada deve ter nota inferior a 10.");
        }
    }

    /**
     * Devolve um erro HTTP 400 quando os dados são inválidos.
     */
    private void badRequest(String message) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }
}
