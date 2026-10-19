
package com.studymatch.controller;

import com.studymatch.dto.EnrollmentRequest;
import com.studymatch.dto.EnrollmentResponse;
import com.studymatch.service.EnrollmentService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST responsável pelas inscrições
 * e pelo histórico académico dos estudantes.
 *
 * Recebe os pedidos HTTP do frontend
 * e encaminha-os para o EnrollmentService.
 */
@RestController
@RequestMapping("/api/students/{studentId}/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * O Spring injeta automaticamente o serviço.
     */
    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    /**
     * Regista uma nova inscrição.
     *
     * POST /api/students/{studentId}/enrollments
     *
     * @PathVariable obtém o ID do estudante do URL.
     * @RequestBody recebe os dados JSON enviados pelo frontend.
     * @ResponseStatus devolve HTTP 201 quando a inscrição é criada.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentResponse createEnrollment(
            @PathVariable Long studentId,
            @RequestBody EnrollmentRequest request) {

        return enrollmentService.createEnrollment(studentId, request);
    }

    /**
     * Consulta o histórico académico de um estudante.
     *
     * GET /api/students/{studentId}/enrollments
     *
     * Devolve todas as inscrições e respetivas tentativas.
     */
    @GetMapping
    public List<EnrollmentResponse> getStudentEnrollments(
            @PathVariable Long studentId) {

        return enrollmentService.getStudentEnrollments(studentId);
    }
}
