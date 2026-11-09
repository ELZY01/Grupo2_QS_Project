package com.studymatch.service;

import com.studymatch.dto.LoginRequest;
import com.studymatch.dto.LoginResponse;
import com.studymatch.dto.RegisterRequest;
import com.studymatch.model.AppUser;
import com.studymatch.model.Role;
import com.studymatch.model.Student;
import com.studymatch.repository.AppUserRepository;
import com.studymatch.repository.StudentRepository;
import com.studymatch.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes do login, do registo e das palavras-passe (Mockito, sem base de dados).
 */
class AuthServiceTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private AppUserRepository users;
    private StudentRepository students;
    private StudentService studentService;
    private TokenService tokens;
    private AuthService service;

    @BeforeEach
    void setUp() {
        users = mock(AppUserRepository.class);
        students = mock(StudentRepository.class);
        studentService = mock(StudentService.class);
        tokens = new TokenService(Duration.ofHours(1));
        service = new AuthService(users, students, studentService, encoder, tokens);
        when(users.save(any(AppUser.class))).thenAnswer(inv -> withId(inv.getArgument(0), 99L));
    }

    @Test
    void logsInWithTheRightPasswordAndReturnsAWorkingToken() {
        AppUser admin = withId(new AppUser("admin", encoder.encode("studymatch"), Role.ADMIN, "Administrador", null), 1L);
        when(users.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(admin));

        LoginResponse response = service.login(new LoginRequest(" admin ", "studymatch"));

        assertEquals(Role.ADMIN, response.role());
        assertEquals(Optional.of(1L), tokens.resolve(response.token()));
    }

    @Test
    void rejectsAWrongPasswordAndAnUnknownUserWithTheSameMessage() {
        AppUser admin = withId(new AppUser("admin", encoder.encode("studymatch"), Role.ADMIN, "Administrador", null), 1L);
        when(users.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(admin));
        when(students.findByEmailIgnoreCaseOrStudentNumberIgnoreCase(anyString(), anyString())).thenReturn(Optional.empty());

        var wrong = assertThrows(ResponseStatusException.class,
                () -> service.login(new LoginRequest("admin", "errada123")));
        var unknown = assertThrows(ResponseStatusException.class,
                () -> service.login(new LoginRequest("ninguem", "errada123")));

        assertEquals(HttpStatus.UNAUTHORIZED, wrong.getStatusCode());
        assertEquals(wrong.getReason(), unknown.getReason());
    }

    @Test
    void aStudentCanLogInWithTheEmail() {
        Student ana = student(7L, "a22001", "ana@ulusofona.pt");
        AppUser account = withId(new AppUser("a22001", encoder.encode("segredo123"), Role.ALUNO, "Ana", 7L), 5L);
        when(users.findByUsernameIgnoreCase("ana@ulusofona.pt")).thenReturn(Optional.empty());
        when(students.findByEmailIgnoreCaseOrStudentNumberIgnoreCase("ana@ulusofona.pt", "ana@ulusofona.pt"))
                .thenReturn(Optional.of(ana));
        when(users.findByStudentId(7L)).thenReturn(Optional.of(account));

        LoginResponse response = service.login(new LoginRequest("ana@ulusofona.pt", "segredo123"));

        assertEquals(Role.ALUNO, response.role());
        assertEquals(7L, response.studentId());
    }

    @Test
    void registersTheStudentWithAHashedPasswordAndStartsTheSession() {
        when(studentService.createStudent(any(Student.class)))
                .thenAnswer(inv -> withStudentId(inv.getArgument(0), 7L));

        LoginResponse response = service.register(
                new RegisterRequest("a22001", "Ana", "ana@ulusofona.pt", "LEI", 2, "segredo123"));

        ArgumentCaptor<AppUser> saved = ArgumentCaptor.forClass(AppUser.class);
        verify(users).save(saved.capture());
        assertEquals(Role.ALUNO, saved.getValue().getRole());
        assertEquals(7L, saved.getValue().getStudentId());
        assertNotEquals("segredo123", saved.getValue().getPasswordHash());
        assertTrue(encoder.matches("segredo123", saved.getValue().getPasswordHash()));
        assertNotNull(tokens.resolve(response.token()).orElse(null));
    }

    @Test
    void rejectsAShortPasswordBeforeCreatingTheStudent() {
        var error = assertThrows(ResponseStatusException.class, () -> service.register(
                new RegisterRequest("a22001", "Ana", "ana@ulusofona.pt", "LEI", 2, "curta")));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(studentService, never()).createStudent(any());
    }

    @Test
    void adminCreatesTheAccountOfAnOldStudentWithoutOne() {
        when(studentService.getStudentById(7L)).thenReturn(student(7L, "a22001", "ana@ulusofona.pt"));
        when(users.findByStudentId(7L)).thenReturn(Optional.empty());

        service.setStudentPassword(7L, "novaPass123");

        ArgumentCaptor<AppUser> saved = ArgumentCaptor.forClass(AppUser.class);
        verify(users).save(saved.capture());
        assertEquals("a22001", saved.getValue().getUsername());
        assertTrue(encoder.matches("novaPass123", saved.getValue().getPasswordHash()));
    }

    @Test
    void adminResetsTheExistingPassword() {
        AppUser account = withId(new AppUser("a22001", encoder.encode("antiga123"), Role.ALUNO, "Ana", 7L), 5L);
        when(studentService.getStudentById(7L)).thenReturn(student(7L, "a22001", "ana@ulusofona.pt"));
        when(users.findByStudentId(7L)).thenReturn(Optional.of(account));

        service.setStudentPassword(7L, "novaPass123");

        assertTrue(encoder.matches("novaPass123", account.getPasswordHash()));
    }

    private static Student student(Long id, String number, String email) {
        return withStudentId(new Student(number, "Ana", email, "LEI", 2), id);
    }

    private static Student withStudentId(Student student, Long id) {
        ReflectionTestUtils.setField(student, "id", id);
        return student;
    }

    private static AppUser withId(AppUser user, Long id) {
        if (user.getId() == null) {
            ReflectionTestUtils.setField(user, "id", id);
        }
        return user;
    }
}
