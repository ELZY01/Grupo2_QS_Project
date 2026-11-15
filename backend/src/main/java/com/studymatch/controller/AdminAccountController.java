package com.studymatch.controller;

import com.studymatch.dto.PasswordRequest;
import com.studymatch.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contas dos alunos geridas pelo admin (só ADMIN, ver SecurityConfig).
 */
@RestController
@RequestMapping("/api/admin/students")
public class AdminAccountController {

    private final AuthService authService;

    public AdminAccountController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * PUT /api/admin/students/{studentId}/password
     * Define a palavra-passe do aluno (cria a conta se ainda não existir). Devolve 204.
     */
    @PutMapping("/{studentId}/password")
    public ResponseEntity<Void> setPassword(@PathVariable Long studentId, @RequestBody PasswordRequest request) {
        authService.setStudentPassword(studentId, request == null ? null : request.password());
        return ResponseEntity.noContent().build();
    }
}
