package com.studymatch.controller;

import com.studymatch.model.Student;
import com.studymatch.service.StudentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller responsável pelos endpoints REST dos estudantes.
 *
 * Recebe os pedidos do frontend e encaminha-os
 * para o StudentService, onde estão as regras de negócio.
 */
@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    /**
     * O Spring injeta automaticamente o StudentService.
     */
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * POST /api/students
     *
     * Regista um novo estudante.
     * Retorna HTTP 201 quando o registo é criado.
     */
    @PostMapping
    public ResponseEntity<Student> createStudent(
            @RequestBody Student student) {

        Student createdStudent = studentService.createStudent(student);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdStudent);
    }

    /**
     * GET /api/students
     *
     * Devolve a lista de estudantes registados.
     */
    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {

        List<Student> students = studentService.getAllStudents();

        return ResponseEntity.ok(students);
    }

    /**
     * GET /api/students/{id}
     *
     * Consulta um estudante através do seu identificador.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(
            @PathVariable Long id) {

        Student student = studentService.getStudentById(id);

        return ResponseEntity.ok(student);
    }

    /**
     * PUT /api/students/{id}
     *
     * Atualiza os dados de um estudante existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(
            @PathVariable Long id,
            @RequestBody Student student) {

        Student updatedStudent =
                studentService.updateStudent(id, student);

        return ResponseEntity.ok(updatedStudent);
    }
}