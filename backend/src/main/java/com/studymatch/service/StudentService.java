
package com.studymatch.service;

import com.studymatch.model.Student;
import com.studymatch.repository.StudentRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Serviço responsável pela lógica de negócio dos estudantes.
 *
 * Esta classe comunica com o StudentRepository
 * e verifica as regras antes de guardar os dados.
 */
@Service
public class StudentService {

    private final StudentRepository studentRepository;

    /**
     * O Spring injeta automaticamente o repositório.
     */
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    /**
     * Regista um novo estudante na base de dados.
     */
    public Student createStudent(Student student) {

        // Impede a utilização de um email já registado.
        if (studentRepository.existsByEmail(student.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este email já está registado."
            );
        }

        // Impede números de estudante duplicados.
        if (studentRepository.existsByStudentNumber(student.getStudentNumber())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este número de estudante já existe."
            );
        }

        // O identificador é gerado automaticamente pela base de dados.
        return studentRepository.save(student);
    }

    /**
     * Devolve todos os estudantes registados.
     */
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    /**
     * Procura um estudante através do seu ID.
     */
    public Student getStudentById(Long id) {

        return studentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Estudante não encontrado."
                ));
    }

    /**
     * Atualiza os dados de um estudante existente.
     */
    public Student updateStudent(Long id, Student updatedStudent) {

        // Confirma que o estudante existe.
        Student student = getStudentById(id);

        // Verifica se o email pertence a outro estudante.
        if (studentRepository.existsByEmailAndIdNot(
                updatedStudent.getEmail(), id)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este email já pertence a outro estudante."
            );
        }

        // Verifica se o número pertence a outro estudante.
        if (studentRepository.existsByStudentNumberAndIdNot(
                updatedStudent.getStudentNumber(), id)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este número já pertence a outro estudante."
            );
        }

        // Atualiza os campos permitidos.
        student.setName(updatedStudent.getName());
        student.setEmail(updatedStudent.getEmail());
        student.setStudentNumber(updatedStudent.getStudentNumber());
        student.setCourse(updatedStudent.getCourse());
        student.setAcademicYear(updatedStudent.getAcademicYear());

        return studentRepository.save(student);
    }
}
