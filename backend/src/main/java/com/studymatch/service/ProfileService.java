package com.studymatch.service;

import com.studymatch.dto.ProfileResponse;
import com.studymatch.dto.ProfileResponse.CompetencyProfile;
import com.studymatch.model.Answer;
import com.studymatch.model.Competency;
import com.studymatch.model.Enrollment;
import com.studymatch.model.QuizSubmission;
import com.studymatch.model.Student;
import com.studymatch.profile.AcademicResult;
import com.studymatch.profile.CompetencyLevel;
import com.studymatch.profile.CompetencyWeight;
import com.studymatch.profile.ProfileCalculator;
import com.studymatch.profile.StudentProfile;
import com.studymatch.repository.AnswerRepository;
import com.studymatch.repository.CompetencyRepository;
import com.studymatch.repository.CourseUnitCompetencyRepository;
import com.studymatch.repository.EnrollmentRepository;
import com.studymatch.repository.QuizSubmissionRepository;
import com.studymatch.repository.StudentRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Constrói o perfil de competências de um estudante (Issue #45).
 *
 * Esta classe só recolhe os dados (estudante, inscrições, pesos e quiz)
 * e entrega-os ao ProfileCalculator, onde estão as regras do ADR-002.
 * O perfil é sempre calculado com os dados atuais: não é guardado (ADR-002, D9).
 */
@Service
public class ProfileService {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CompetencyRepository competencyRepository;
    private final CourseUnitCompetencyRepository courseUnitCompetencyRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final AnswerRepository answerRepository;

    private final ProfileCalculator calculator = new ProfileCalculator();

    public ProfileService(
            StudentRepository studentRepository,
            EnrollmentRepository enrollmentRepository,
            CompetencyRepository competencyRepository,
            CourseUnitCompetencyRepository courseUnitCompetencyRepository,
            QuizSubmissionRepository quizSubmissionRepository,
            AnswerRepository answerRepository) {

        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.competencyRepository = competencyRepository;
        this.courseUnitCompetencyRepository = courseUnitCompetencyRepository;
        this.quizSubmissionRepository = quizSubmissionRepository;
        this.answerRepository = answerRepository;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long studentId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Estudante não encontrado."
                ));

        // 1. Competências (pela ordem da base de dados).
        List<Competency> competencies = competencyRepository.findAllByOrderByIdAsc();
        List<String> codes = competencies.stream().map(Competency::getCode).toList();

        // 2. Todas as tentativas do estudante (o calculador escolhe a melhor aprovada).
        List<AcademicResult> results = enrollmentRepository
                .findByStudentIdOrderByCourseUnitIdAscAttemptNumberAsc(studentId)
                .stream()
                .map(ProfileService::toAcademicResult)
                .toList();

        // 3. Pesos UC → competência.
        List<CompetencyWeight> weights = courseUnitCompetencyRepository
                .findAllWithCourseUnitAndCompetency()
                .stream()
                .map(cuc -> new CompetencyWeight(
                        cuc.getCompetency().getCode(),
                        cuc.getCourseUnit().getCode(),
                        cuc.getWeight()))
                .toList();

        // 4. Resultado da submissão de quiz mais recente, por código de competência.
        Optional<QuizSubmission> latestQuiz =
                quizSubmissionRepository.findTopByStudentIdOrderBySubmittedAtDesc(studentId);
        Map<String, Double> quizLevels = latestQuiz
                .map(submission -> quizLevelsByCompetencyCode(submission.getId()))
                .orElseGet(Map::of);

        // 5. Regras do perfil (ADR-002).
        StudentProfile profile = calculator.calculate(codes, results, weights, quizLevels);

        Map<String, String> namesByCode = competencies.stream()
                .collect(Collectors.toMap(Competency::getCode, Competency::getName));

        List<CompetencyProfile> competencyProfiles = profile.competencies().stream()
                .map(level -> toCompetencyProfile(level, namesByCode.get(level.competencyCode())))
                .toList();

        return new ProfileResponse(
                student.getId(),
                student.getName(),
                profile.completedCourseUnits(),
                profile.quizWeight(),
                latestQuiz.isPresent(),
                competencyProfiles);
    }

    private static AcademicResult toAcademicResult(Enrollment enrollment) {
        return new AcademicResult(
                enrollment.getCourseUnit().getCode(),
                enrollment.getCourseUnit().getName(),
                enrollment.getGrade());
    }

    /** Média das respostas (1–5) agrupadas pelo código da competência de cada pergunta. */
    private Map<String, Double> quizLevelsByCompetencyCode(Long submissionId) {
        Map<String, int[]> sumAndCount = new HashMap<>();
        for (Answer answer : answerRepository.findBySubmissionId(submissionId)) {
            String code = answer.getQuestion().getCompetency().getCode();
            int[] acc = sumAndCount.computeIfAbsent(code, k -> new int[2]);
            acc[0] += answer.getValue();
            acc[1]++;
        }
        Map<String, Double> levels = new HashMap<>();
        sumAndCount.forEach((code, acc) -> levels.put(code, (double) acc[0] / acc[1]));
        return levels;
    }

    private static CompetencyProfile toCompetencyProfile(CompetencyLevel level, String name) {
        return new CompetencyProfile(
                level.competencyCode(),
                name,
                level.level(),
                level.origin(),
                level.quizWeight(),
                level.quizLevel(),
                level.historyLevel(),
                level.contributions());
    }
}
