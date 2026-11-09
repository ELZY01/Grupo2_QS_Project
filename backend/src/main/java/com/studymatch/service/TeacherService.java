package com.studymatch.service;

import com.studymatch.dto.CourseUnitResponse;
import com.studymatch.dto.StudentSummary;
import com.studymatch.dto.TeacherCourseUnitResponse;
import com.studymatch.repository.CourseUnitRepository;
import com.studymatch.repository.CourseUnitStudentRepository;
import com.studymatch.repository.CourseUnitTeacherRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Área do docente (R3): só as UCs atribuídas e só os alunos inscritos nelas.
 * As estatísticas de cada aluno vêm do perfil e do histórico, que o docente
 * consegue ler para os alunos das suas UCs (ver StudentAccess).
 */
@Service
public class TeacherService {

    private final CourseUnitRepository courseUnits;
    private final CourseUnitTeacherRepository teachers;
    private final CourseUnitStudentRepository enrolled;

    public TeacherService(CourseUnitRepository courseUnits,
                          CourseUnitTeacherRepository teachers,
                          CourseUnitStudentRepository enrolled) {
        this.courseUnits = courseUnits;
        this.teachers = teachers;
        this.enrolled = enrolled;
    }

    /**
     * UCs atribuídas ao docente, por ordem alfabética.
     */
    public List<TeacherCourseUnitResponse> myCourseUnits(Long teacherId) {
        return teachers.findByTeacherIdOrderByCourseUnitNameAsc(teacherId).stream()
                .map(ct -> new TeacherCourseUnitResponse(
                        CourseUnitResponse.from(ct.getCourseUnit()),
                        enrolled.countByCourseUnitId(ct.getCourseUnit().getId())))
                .toList();
    }

    /**
     * Alunos inscritos numa UC do docente. UC de outro docente → 403 (R7).
     */
    public List<StudentSummary> studentsOf(Long teacherId, Long courseUnitId) {
        if (!courseUnits.existsById(courseUnitId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidade curricular não encontrada.");
        }
        if (!teachers.existsByCourseUnitIdAndTeacherId(courseUnitId, teacherId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta UC não te está atribuída.");
        }
        return enrolled.findByCourseUnitIdOrderByStudentNameAsc(courseUnitId).stream()
                .map(cs -> StudentSummary.from(cs.getStudent()))
                .toList();
    }
}
