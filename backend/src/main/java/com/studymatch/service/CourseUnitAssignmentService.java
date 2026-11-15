package com.studymatch.service;

import com.studymatch.dto.CourseUnitAssignmentsResponse;
import com.studymatch.dto.CourseUnitResponse;
import com.studymatch.dto.CreateTeacherRequest;
import com.studymatch.dto.StudentSummary;
import com.studymatch.dto.TeacherResponse;
import com.studymatch.model.AppUser;
import com.studymatch.model.CourseUnit;
import com.studymatch.model.CourseUnitStudent;
import com.studymatch.model.CourseUnitTeacher;
import com.studymatch.model.Role;
import com.studymatch.model.Student;
import com.studymatch.repository.AppUserRepository;
import com.studymatch.repository.CourseUnitRepository;
import com.studymatch.repository.CourseUnitStudentRepository;
import com.studymatch.repository.CourseUnitTeacherRepository;
import com.studymatch.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Atribuições feitas pelo admin (só ADMIN chega aqui, ver SecurityConfig):
 * - R1: inscrever e remover alunos numa UC;
 * - R2: associar e remover docentes de uma UC;
 * - criar contas de docente.
 */
@Service
public class CourseUnitAssignmentService {

    private final CourseUnitRepository courseUnits;
    private final StudentRepository students;
    private final AppUserRepository users;
    private final CourseUnitTeacherRepository teachers;
    private final CourseUnitStudentRepository enrolled;
    private final PasswordEncoder passwordEncoder;

    public CourseUnitAssignmentService(CourseUnitRepository courseUnits,
                                       StudentRepository students,
                                       AppUserRepository users,
                                       CourseUnitTeacherRepository teachers,
                                       CourseUnitStudentRepository enrolled,
                                       PasswordEncoder passwordEncoder) {
        this.courseUnits = courseUnits;
        this.students = students;
        this.users = users;
        this.teachers = teachers;
        this.enrolled = enrolled;
        this.passwordEncoder = passwordEncoder;
    }

    // --- Docentes -----------------------------------------------------------

    public List<TeacherResponse> listTeachers() {
        return users.findByRoleOrderByDisplayNameAsc(Role.DOCENTE).stream().map(TeacherResponse::from).toList();
    }

    @Transactional
    public TeacherResponse createTeacher(CreateTeacherRequest request) {
        String username = request == null || request.username() == null ? "" : request.username().trim();
        String name = request == null || request.displayName() == null ? "" : request.displayName().trim();
        if (username.isEmpty() || name.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome e o utilizador do docente são obrigatórios.");
        }
        AuthService.validatePassword(request.password());
        if (users.existsByUsernameIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma conta com este utilizador.");
        }
        AppUser teacher = users.save(new AppUser(
                username, passwordEncoder.encode(request.password()), Role.DOCENTE, name, null));
        return TeacherResponse.from(teacher);
    }

    // --- Consulta -----------------------------------------------------------

    public CourseUnitAssignmentsResponse getAssignments(Long courseUnitId) {
        CourseUnit unit = findCourseUnit(courseUnitId);
        List<TeacherResponse> teacherList = teachers.findByCourseUnitIdOrderByTeacherDisplayNameAsc(courseUnitId)
                .stream().map(ct -> TeacherResponse.from(ct.getTeacher())).toList();
        List<StudentSummary> studentList = enrolled.findByCourseUnitIdOrderByStudentNameAsc(courseUnitId)
                .stream().map(cs -> StudentSummary.from(cs.getStudent())).toList();
        return new CourseUnitAssignmentsResponse(CourseUnitResponse.from(unit), teacherList, studentList);
    }

    // --- R2: docentes ---------------------------------------------------------

    @Transactional
    public CourseUnitAssignmentsResponse assignTeacher(Long courseUnitId, Long teacherId) {
        CourseUnit unit = findCourseUnit(courseUnitId);
        AppUser teacher = users.findById(teacherId == null ? -1L : teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Docente não encontrado."));
        if (teacher.getRole() != Role.DOCENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esta conta não é de um docente.");
        }
        if (teachers.existsByCourseUnitIdAndTeacherId(courseUnitId, teacherId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este docente já está associado a esta UC.");
        }
        teachers.save(new CourseUnitTeacher(unit, teacher));
        return getAssignments(courseUnitId);
    }

    @Transactional
    public CourseUnitAssignmentsResponse removeTeacher(Long courseUnitId, Long teacherId) {
        findCourseUnit(courseUnitId);
        CourseUnitTeacher link = teachers.findByCourseUnitIdAndTeacherId(courseUnitId, teacherId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Este docente não está associado a esta UC."));
        teachers.delete(link);
        return getAssignments(courseUnitId);
    }

    // --- R1: alunos -----------------------------------------------------------

    @Transactional
    public CourseUnitAssignmentsResponse enrollStudent(Long courseUnitId, Long studentId) {
        CourseUnit unit = findCourseUnit(courseUnitId);
        Student student = students.findById(studentId == null ? -1L : studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudante não encontrado."));
        if (enrolled.existsByCourseUnitIdAndStudentId(courseUnitId, studentId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este aluno já está inscrito nesta UC.");
        }
        enrolled.save(new CourseUnitStudent(unit, student));
        return getAssignments(courseUnitId);
    }

    @Transactional
    public CourseUnitAssignmentsResponse removeStudent(Long courseUnitId, Long studentId) {
        findCourseUnit(courseUnitId);
        CourseUnitStudent link = enrolled.findByCourseUnitIdAndStudentId(courseUnitId, studentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Este aluno não está inscrito nesta UC."));
        enrolled.delete(link);
        return getAssignments(courseUnitId);
    }

    private CourseUnit findCourseUnit(Long id) {
        return courseUnits.findById(id == null ? -1L : id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Unidade curricular não encontrada."));
    }
}
