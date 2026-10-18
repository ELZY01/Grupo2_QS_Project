package com.studymatch.service;

import com.studymatch.model.Student;
import com.studymatch.repository.StudentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários do serviço de estudantes.
 *
 * Utilizamos Mockito para simular o repositório.
 * Assim, os testes não alteram a base de dados Supabase.
 */
class StudentServiceTest {

    private StudentRepository repository;
    private StudentService service;

    /**
     * Prepara um repositório simulado antes de cada teste.
     */
    @BeforeEach
    void preparar() {
        repository = mock(StudentRepository.class);
        service = new StudentService(repository);
    }

    /**
     * Cria um estudante fictício válido para os testes.
     */
    private Student criarEstudanteValido() {
        Student student = new Student();

        student.setStudentNumber("20260003");
        student.setName("Estudante Teste");
        student.setEmail("teste3@example.com");
        student.setCourse("Engenharia Informatica");
        student.setAcademicYear(1);

        return student;
    }

    /**
     * Verifica se um estudante válido é guardado.
     */
    @Test
    void deveCriarEstudanteValido() {

        Student student = criarEstudanteValido();

        when(repository.save(any(Student.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Student resultado = service.createStudent(student);

        assertEquals("Estudante Teste", resultado.getName());

        // Confirma que o método save foi chamado uma vez.
        verify(repository, times(1)).save(student);
    }

    /**
     * Verifica se um email duplicado provoca HTTP 409.
     */
    @Test
    void deveRejeitarEmailDuplicado() {

        Student student = criarEstudanteValido();

        when(repository.existsByEmail(student.getEmail()))
                .thenReturn(true);

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> service.createStudent(student)
        );

        assertEquals(HttpStatus.CONFLICT, erro.getStatusCode());

        // Um estudante inválido não pode ser guardado.
        verify(repository, never()).save(any(Student.class));
    }

    /**
     * Verifica se um nome vazio provoca HTTP 400.
     */
    @Test
    void deveRejeitarNomeVazio() {

        Student student = criarEstudanteValido();
        student.setName("");

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> service.createStudent(student)
        );

        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());

        verify(repository, never()).save(any(Student.class));
    }

    /**
     * Verifica se um estudante inexistente provoca HTTP 404.
     */
    @Test
    void deveRejeitarEstudanteInexistente() {

        when(repository.findById(999L))
                .thenReturn(Optional.empty());

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> service.getStudentById(999L)
        );

        assertEquals(HttpStatus.NOT_FOUND, erro.getStatusCode());
    }
}