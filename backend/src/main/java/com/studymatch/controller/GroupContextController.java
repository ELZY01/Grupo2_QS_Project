
package com.studymatch.controller;

import com.studymatch.dto.CreateGroupContextRequest;
import com.studymatch.dto.GeneratedGroupResponse;
import com.studymatch.dto.GroupContextResponse;
import com.studymatch.grouping.ImpossibleGroupConfigurationException;
import com.studymatch.service.GroupGenerationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller responsável pela gestão e geração de grupos.
 * Issue #69.
 */
@RestController
@RequestMapping("/api/group-contexts")
public class GroupContextController {

    private final GroupGenerationService groupGenerationService;

    public GroupContextController(
            GroupGenerationService groupGenerationService) {
        this.groupGenerationService = groupGenerationService;
    }

    /**
     * Cria um contexto de formação de grupos.
     */
    @PostMapping
    public ResponseEntity<GroupContextResponse> createContext(
            @RequestBody CreateGroupContextRequest request) {

        GroupContextResponse response =
                groupGenerationService.createContext(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lista todos os contextos e respetivas gerações.
     */
    @GetMapping
    public ResponseEntity<List<GroupContextResponse>> getAllContexts() {

        return ResponseEntity.ok(
                groupGenerationService.getAllContexts()
        );
    }

    /**
     * Gera uma nova versão dos grupos de um contexto.
     */
    @PostMapping("/{id}/generate")
    public ResponseEntity<List<GeneratedGroupResponse>> generateGroups(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                groupGenerationService.generateGroups(id)
        );
    }

    /**
     * Consulta os grupos gerados num contexto.
     */
    @GetMapping("/{id}/groups")
    public ResponseEntity<List<GeneratedGroupResponse>> getGroups(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                groupGenerationService.getGroups(id)
        );
    }

    /**
     * Configuração impossível (ex.: 7 alunos) → 400 com a mensagem, como o frontend espera.
     * Sem isto a exceção chegava ao cliente como 500 (Issue #113, ADR-003 D1).
     * Mesmo formato dos erros do SecurityConfig e do TeacherGroupController.
     */
    @ExceptionHandler(ImpossibleGroupConfigurationException.class)
    ResponseEntity<Map<String, Object>> impossibleConfiguration(ImpossibleGroupConfigurationException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("status", HttpStatus.BAD_REQUEST.value(), "message", e.getMessage()));
    }
}
