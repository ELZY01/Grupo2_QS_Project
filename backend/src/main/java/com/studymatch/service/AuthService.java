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
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

/**
 * Login, registo de alunos e gestão das palavras-passe.
 */
@Service
public class AuthService {

    public static final int MIN_PASSWORD_LENGTH = 8;

    private static final String WRONG_CREDENTIALS = "Utilizador ou palavra-passe errados.";

    private final AppUserRepository userRepository;
    private final StudentRepository studentRepository;
    private final StudentService studentService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(AppUserRepository userRepository,
                       StudentRepository studentRepository,
                       StudentService studentService,
                       PasswordEncoder passwordEncoder,
                       TokenService tokenService) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.studentService = studentService;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    /**
     * Inicia sessão. O aluno pode usar o número de estudante ou o email.
     * A mensagem de erro é a mesma para utilizador inexistente e palavra-passe errada.
     */
    public LoginResponse login(LoginRequest request) {
        String username = request == null || request.username() == null ? "" : request.username().trim();
        String password = request == null || request.password() == null ? "" : request.password();

        AppUser user = findAccount(username)
                .filter(u -> passwordEncoder.matches(password, u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, WRONG_CREDENTIALS));

        return sessionFor(user);
    }

    /**
     * Regista um aluno novo e a respetiva conta, e inicia logo a sessão.
     */
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Os dados do registo são obrigatórios.");
        }
        validatePassword(request.password());

        Student student = studentService.createStudent(new Student(
                request.studentNumber(), request.name(), request.email(), request.course(), request.academicYear()));

        if (userRepository.existsByUsernameIgnoreCase(student.getStudentNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma conta com este número de estudante.");
        }

        AppUser user = userRepository.save(new AppUser(
                student.getStudentNumber(),
                passwordEncoder.encode(request.password()),
                Role.ALUNO,
                student.getName(),
                student.getId()));

        return sessionFor(user);
    }

    /**
     * O admin define (ou redefine) a palavra-passe de um aluno.
     * Se o aluno ainda não tiver conta (alunos antigos), a conta é criada.
     */
    @Transactional
    public void setStudentPassword(Long studentId, String password) {
        validatePassword(password);
        Student student = studentService.getStudentById(studentId);
        String hash = passwordEncoder.encode(password);

        AppUser user = userRepository.findByStudentId(studentId)
                .orElseGet(() -> new AppUser(student.getStudentNumber(), hash, Role.ALUNO, student.getName(), studentId));
        user.setPasswordHash(hash);
        user.setDisplayName(student.getName());
        userRepository.save(user);
    }

    /**
     * Termina a sessão do token indicado.
     */
    public void logout(String token) {
        tokenService.revoke(token);
    }

    /**
     * Valida a regra mínima da palavra-passe.
     */
    public static void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A palavra-passe tem de ter pelo menos " + MIN_PASSWORD_LENGTH + " caracteres.");
        }
    }

    private Optional<AppUser> findAccount(String username) {
        if (username.isEmpty()) {
            return Optional.empty();
        }
        Optional<AppUser> direct = userRepository.findByUsernameIgnoreCase(username);
        if (direct.isPresent()) {
            return direct;
        }
        // O aluno pode entrar com o email ou com o número atual (se o admin o mudou).
        return studentRepository.findByEmailIgnoreCaseOrStudentNumberIgnoreCase(username, username)
                .flatMap(student -> userRepository.findByStudentId(student.getId()));
    }

    private LoginResponse sessionFor(AppUser user) {
        String token = tokenService.issue(user.getId());
        return new LoginResponse(token, user.getRole(), user.getId(), user.getStudentId(), user.getDisplayName());
    }
}
